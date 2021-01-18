package com.mactavish.sevenz4s.updater

import java.io.{Closeable, InputStream, OutputStream}
import java.nio.file.Path

import com.mactavish.sevenz4s.{CompressionEntry, EntryProxy, SevenZ4S, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.OutItemFactory

import scala.collection.mutable

trait AbstractArchiveUpdater[E <: AbstractArchiveUpdater[E]] {
  this: E =>

  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  protected val format: ArchiveFormat
  private var source: Either[Path, InputStream] = _
  private var dst: Either[Path, OutputStream] = _
  private var password: String = _

  def from(src: Either[Path, InputStream]): E = {
    if (src == null) throw SevenZ4SException("`from` doesn't accept null parameter")
    this.source = src
    this.source match {
      case Left(path) =>
        // if `src` comes from file, then `dst` is set the same as `src` by default
        if (this.dst == null) this.dst = Left(path)
      case Right(_) =>
    }
    this
  }

  def towards(dst: Either[Path, OutputStream]): E = {
    if (dst == null) throw SevenZ4SException("`towards` doesn't accept null parameter")

    this.dst = dst
    this
  }

  def withPassword(passwd: String): E = {
    if (passwd == null) throw SevenZ4SException("null passwd is meaningless")
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
      val contentMap = mutable.HashMap[Int, Either[Path, InputStream]]()
      val entryStreams = mutable.HashSet[Closeable]()

      archive.updateItems(dst, itemNum, new DefaultIOutCreateCallback {
        override def getItemInformation(
                                         i: Int,
                                         outItemFactory: OutItemFactory[TItem]
                                       ): TItem = {
          val originalItem = outItemFactory.createOutItemAndCloneProperties(i)
          val originalEntry = adaptItemToEntry(originalItem)
          val newEntry = f(originalEntry)
          if (newEntry == null) throw SevenZ4SException("mustn't return empty entry")
          val newItem = adaptEntryToItem(newEntry, originalItem)
          // properties changed
          // TODO: For now, if `newEntry.source` is set, it'll also be marked as
          // `properties changed` since we determine it through simple comparison.
          // Though it is a big deal, we may improve it later.
          if (newEntry != originalEntry) newItem.setUpdateIsNewProperties(true)
          // content changed
          if (newEntry.source != null) {
            newItem.setUpdateIsNewData(true)
            contentMap(i) = newEntry.source
          }
          newItem
        }

        /**
         * `getStream` is called after all(`numEntry` times) `getItemInformation` calls,
         * but note that, it could be called for times fewer than `numEntry`,
         * since it'll only be called on `i` whose corresponding item triggered `setUpdateIsNewData`,
         * meaning `i` can be discontinuous (and truly so in most of time).
         *
         * @param i index of entry
         * @return where 7Z engine gets data stream
         */
        override def getStream(i: Int): ISequentialInStream = {
          val stream = SevenZ4S.open(contentMap(i))
          entryStreams.add(stream)
          stream
        }

        override def cryptoGetTextPassword(): String = password
      })

      entryStreams.foreach(_.close()) // close user-provided streams
      this
  }

  def ++=(entries: Seq[TEntry]): E = append(entries)

  def append(entries: Seq[TEntry]): E = withArchive {
    (itemNum, archive, dst) =>
      val entryStreams = mutable.HashSet[Closeable]()
      val entryProxy = new EntryProxy(entries)

      // `updateItems` takes the number of items in NEW archive as a parameter
      archive.updateItems(dst, itemNum + entries.size, new DefaultIOutCreateCallback {
        override def getItemInformation(i: Int, outItemFactory: OutItemFactory[TItem]): TItem = {
          if (i < itemNum) outItemFactory.createOutItem(i)
          else {
            entryProxy.next() match {
              case Some(entry) => adaptEntryToItem(entry, outItemFactory.createOutItem())
              case None =>
                throw SevenZ4SException(s"only ${i - itemNum} entries provided, ${entries.size} expected")
            }
          }
        }

        override def getStream(i: Int): ISequentialInStream = {
          if (!entryProxy.hasNext) entryProxy.reset() // reset cursor at the beginning
          if (i < itemNum) null // existed items remain intact
          else {
            entryProxy.nextSource() match {
              case Some(src) =>
                entryStreams.add(src)
                src
              case None =>
                throw SevenZ4SException("not enough entries containing source are provided")
            }
          }
        }

        override def cryptoGetTextPassword(): String = password
      })

      // withArchive can't help with user-provided streams, close it here
      entryStreams.foreach(_.close())
      this
  }

  def +=(entry: TEntry): E = append(entry)

  def append(entry: TEntry): E = append(Seq(entry))

  protected def adaptItemToEntry(item: TItem): TEntry

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem

  /**
   * `DefaultIOutCreateCallback` extends `IOutCreateCallback[TItem]`
   * and `ICryptoGetTextPassword`, and it implements progress related behavior
   * with simply doing nothing.
   */
  trait DefaultIOutCreateCallback extends IOutCreateCallback[TItem] with ICryptoGetTextPassword {
    override def setOperationResult(b: Boolean): Unit = {}

    override def setTotal(l: Long): Unit = {}

    override def setCompleted(l: Long): Unit = {}
  }

  /**
   * `withArchive` opens `IOutUpdateArchive` and supplies number of items, archive itself and
   * `IOutStream`. Most importantly, it closes resources afterwards so that they can be reused.
   */
  private def withArchive[R](f: (Int, IOutUpdateArchive[TItem], IOutStream) => R): R = {
    val fromStream: IInStream = SevenZ4S.open(this.source)
    val outStream: IOutStream with Closeable = SevenZ4S.open(this.dst)

    val from = SevenZip.openInArchive(format, fromStream)
    val to = from.getConnectedOutArchive
    val res = f(from.getNumberOfItems, to.asInstanceOf[IOutUpdateArchive[TItem]], outStream)

    outStream.close()
    // InArchive needs to be closed, connected OutArchive will close automatically
    from.close()
    fromStream.close()

    res
  }
}

