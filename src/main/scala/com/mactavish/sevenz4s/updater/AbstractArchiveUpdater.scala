package com.mactavish.sevenz4s.updater

import java.io.{File, RandomAccessFile}

import com.mactavish.sevenz4s.{CompressionEntry, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.{OutItemFactory, RandomAccessFileInStream, RandomAccessFileOutStream}
import net.sf.sevenzipjbinding.util.ByteArrayStream

import scala.collection.mutable

trait AbstractArchiveUpdater[E <: AbstractArchiveUpdater[E]] {
  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  protected val format: ArchiveFormat
  private var source: Either[File, Array[Byte]] = _
  //private var fromArchive: IInArchive = _
  //private var towardsArchive: IOutUpdateArchive[TItem] = _
  private var dst: Either[File, Array[Byte]] = _

  def from(src: File): E = {
    this.source = Left(src)
    if (this.dst == null) this.dst = this.source
    // down-cast to actual ArchiveUpdater in order to
    // enable chain methods calling on concrete ArchiveUpdater.
    this.asInstanceOf[E]
  }

  def from(src: Array[Byte]): E = {
    this.source = Right(src)
    if (this.dst == null) this.dst = this.source
    // down-cast to actual ArchiveUpdater in order to
    // enable chain methods calling on concrete ArchiveUpdater.
    this.asInstanceOf[E]
  }

  def towards(dst: File): E = {
    this.dst = Left(dst)
    this.asInstanceOf[E]
  }

  def towards(dst: Array[Byte]): E = {
    this.dst = Right(dst)
    this.asInstanceOf[E]
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
            val item = outItemFactory.createOutItemAndCloneProperties(i)
            val entry = adaptItemToEntry(item)
            if (pred(entry)) {
              removalCnt += 1
              outItemFactory.createOutItem(i + removalCnt)
            } else {
              item
            }
          }

          // just removal, nothing to supply
          override def getStream(i: Int): ISequentialInStream = null
        })
        this.asInstanceOf[E]
    }
  }

  def update(f: TEntry => TEntry): E = withArchive {
    (itemNum, archive, dst) =>
      archive.updateItems(dst, itemNum, new DefaultIOutCreateCallback {
        private val contentMap = mutable.HashMap[Int, ISequentialInStream]()

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
      })
      this.asInstanceOf[E]
  }

  /**
   * withArchive opens IOutUpdateArchive and supplies number of items, archive itself and
   * IOutStream. Most importantly, it closes resources afterwards so that they can be reused.
   */
  private def withArchive[R](f: (Int, IOutUpdateArchive[TItem], IOutStream) => R): R = {
    val (fromStream, fromStreamCloser: AutoCloseable) = this.source match {
      case Left(file) =>
        // must open in "r" mode
        val f = new RandomAccessFile(file, "r")
        new RandomAccessFileInStream(f) -> f
      case Right(array) =>
        // generate ByteArrayStream without copying and stored data length limit
        val a = new ByteArrayStream(array, false, Int.MaxValue)
        a -> a
    }
    val (outStream, outStreamCloser: AutoCloseable) = this.dst match {
      case Left(file) =>
        // must open in "rw" mode
        val f = new RandomAccessFile(file, "rw")
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

  def ++=(entries: Seq[TEntry]): E = append(entries)

  def append(entries: Seq[TEntry]): E = withArchive {
    (itemNum, archive, dst) =>
      archive.updateItems(dst, itemNum + entries.size, new DefaultIOutCreateCallback {
        private var cur = entries

        override def getItemInformation(i: Int, outItemFactory: OutItemFactory[TItem]): TItem = {
          if (i < itemNum) outItemFactory.createOutItem(i)
          else {
            val item = adaptEntryToItem(cur.head, outItemFactory.createOutItem())
            cur = cur.tail
            item
          }
        }

        override def getStream(i: Int): ISequentialInStream = {
          if (i == 0) cur = entries // reset cursor
          if (i < itemNum) null
          else {
            val stream = cur.head.source
            cur = cur.tail
            stream
          }
        }
      })
      this.asInstanceOf[E]
  }

  def +=(entry: TEntry): E = append(entry)

  def append(entry: TEntry): E = append(Seq(entry))

  protected def adaptItemToEntry(item: TItem): TEntry

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem

  trait DefaultIOutCreateCallback extends IOutCreateCallback[TItem] {
    override def setOperationResult(b: Boolean): Unit = {}

    override def setTotal(l: Long): Unit = {}

    override def setCompleted(l: Long): Unit = {}
  }

}

