package com.mactavish.sevenz4s.updater

import java.io.RandomAccessFile
import java.nio.file.Path

import com.mactavish.sevenz4s.{CompressionEntry, EntryProxy, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.{OutItemFactory, RandomAccessFileInStream, RandomAccessFileOutStream}
import net.sf.sevenzipjbinding.util.ByteArrayStream

import scala.collection.mutable

trait AbstractArchiveUpdater[E <: AbstractArchiveUpdater[E]] {
  this: E =>

  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  protected val format: ArchiveFormat
  private var source: Either[Path, Array[Byte]] = _
  //private var fromArchive: IInArchive = _
  //private var towardsArchive: IOutUpdateArchive[TItem] = _
  private var dst: Either[Path, Array[Byte]] = _
  private var password: String = _

  def from(src: Path): E = {
    this.source = Left(src)
    if (this.dst == null) this.dst = this.source
    // down-cast to actual ArchiveUpdater in order to
    // enable chain methods calling on concrete ArchiveUpdater.
    this
  }

  def from(src: Array[Byte]): E = {
    this.source = Right(src)
    if (this.dst == null) this.dst = this.source
    // down-cast to actual ArchiveUpdater in order to
    // enable chain methods calling on concrete ArchiveUpdater.
    this
  }

  def towards(dst: Path): E = {
    this.dst = Left(dst)
    this
  }

  def towards(dst: Array[Byte]): E = {
    this.dst = Right(dst)
    this
  }

  def withPassword(passwd: String): E = {
    this.password = passwd
    this
  }

  def -=(entry: TEntry): E = remove(entry)

  def remove(entry: TEntry): E = removeWhere(_ == entry)

  def removeWhere(pred: TEntry => Boolean): E = {
    val entriesToRemove = mutable.Set[CompressionEntry]()
    // find all entries to be removed first, as we need to know the total number
    update {
      entry =>
        if (pred(entry)) entriesToRemove += entry
        // simple traversal, update nothing
        entry
    }

    withArchive {
      (itemNum, archive, dst) =>
        archive.updateItems(dst, itemNum - entriesToRemove.size, new DefaultIOutCreateCallback {
          private var removalCnt = 0

          override def getItemInformation(i: Int, outItemFactory: OutItemFactory[TItem]): TItem = {
            val item = outItemFactory.createOutItemAndCloneProperties(i + removalCnt)
            val entry = adaptItemToEntry(item)
            if (entriesToRemove contains entry)
              removalCnt += 1
            outItemFactory.createOutItem(i + removalCnt)
          }

          override def cryptoGetTextPassword(): String = password

          // just removal, nothing to supply
          override def getStream(i: Int): ISequentialInStream = null
        })
        this
    }
  }

  def update(f: TEntry => TEntry): E = withArchive {
    (itemNum, archive, dst) =>
      val contentMap = mutable.HashMap[Int, ISequentialInStream]()

      archive.updateItems(dst, itemNum, new DefaultIOutCreateCallback {
        override def getItemInformation(
                                         i: Int,
                                         outItemFactory: OutItemFactory[TItem]
                                       ): TItem = {
          val originalItem = outItemFactory.createOutItemAndCloneProperties(i)
          //outItemFactory.createOutItem()
          val originalEntry = adaptItemToEntry(originalItem)
          val newEntry = f(originalEntry)
          if (newEntry == null) throw SevenZ4SException("mustn't return empty entry")
          val newItem = adaptEntryToItem(newEntry, originalItem)
          // properties changed
          if (newEntry != originalEntry) newItem.setUpdateIsNewProperties(true)
          // content changed
          if (newEntry.source != null) {
            newItem.setUpdateIsNewData(true)
            contentMap(i) = newEntry.source
          }
          newItem
        }

        override def getStream(i: Int): ISequentialInStream = {
          contentMap(i)
        }

        override def cryptoGetTextPassword(): String = password
      })

      contentMap.values.foreach(_.close()) // close user-provided streams
      this
  }

  def ++=(entries: Seq[TEntry]): E = append(entries)

  def append(entries: Seq[TEntry]): E = withArchive {
    val entryProxy = new EntryProxy(entries)
    (itemNum, archive, dst) =>
      archive.updateItems(dst, itemNum + entries.size, new DefaultIOutCreateCallback {
        override def getItemInformation(i: Int, outItemFactory: OutItemFactory[TItem]): TItem = {
          if (i < itemNum) outItemFactory.createOutItem(i)
          else {
            entryProxy.next() match {
              case Some(entry) => adaptEntryToItem(entry, outItemFactory.createOutItem())
              case None =>
                throw SevenZ4SException(s"only ${i - entries.size} entries provided, $itemNum expected")
            }
          }
        }

        override def getStream(i: Int): ISequentialInStream = {
          if (!entryProxy.hasNext) entryProxy.reset() // reset cursor at the beginning
          if (i < itemNum) null // existed items remain intact
          else {
            entryProxy.nextSource() match {
              case Some(src) => src
              case None =>
                throw SevenZ4SException("not enough entries containing source are provided")
            }
          }
        }

        override def cryptoGetTextPassword(): String = password
      })
      this
  }

  def +=(entry: TEntry): E = append(entry)

  def append(entry: TEntry): E = append(Seq(entry))

  protected def adaptItemToEntry(item: TItem): TEntry

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem

  trait DefaultIOutCreateCallback extends IOutCreateCallback[TItem] with ICryptoGetTextPassword {
    override def setOperationResult(b: Boolean): Unit = {}

    override def setTotal(l: Long): Unit = {}

    override def setCompleted(l: Long): Unit = {}
  }

  /**
   * withArchive opens IOutUpdateArchive and supplies number of items, archive itself and
   * IOutStream. Most importantly, it closes resources afterwards so that they can be reused.
   */
  private def withArchive[R](f: (Int, IOutUpdateArchive[TItem], IOutStream) => R): R = {
    val (fromStream, fromStreamCloser: AutoCloseable) = this.source match {
      case Left(file) =>
        // must open in "r" mode
        val f = new RandomAccessFile(file.toFile, "r")
        new RandomAccessFileInStream(f) -> f
      case Right(array) =>
        // generate ByteArrayStream without copying and stored data length limit
        val a = new ByteArrayStream(array, false, Int.MaxValue)
        a -> a
    }
    val (outStream, outStreamCloser: AutoCloseable) = this.dst match {
      case Left(file) =>
        // must open in "rw" mode
        val f = new RandomAccessFile(file.toFile, "rw")
        new RandomAccessFileOutStream(f) -> f
      case Right(array) =>
        val a = new ByteArrayStream(array, false, Int.MaxValue)
        a -> a
    }

    val from = SevenZip.openInArchive(format, fromStream)
    val to = from.getConnectedOutArchive
    val res = f(from.getNumberOfItems, to.asInstanceOf[IOutUpdateArchive[TItem]], outStream)

    outStreamCloser.close()
    // InArchive needs to be closed, connected OutArchive will close automatically
    from.close()
    fromStreamCloser.close()
    res
  }
}

