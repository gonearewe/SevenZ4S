package com.mactavish.sevenz4s.updater

import com.mactavish.sevenz4s.{CompressionEntry, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.OutItemFactory

import scala.collection.mutable

trait AbstractArchiveUpdater[E <: AbstractArchiveUpdater[E]] {
  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  protected val format: ArchiveFormat
  private var source: IInStream = _
  //private var fromArchive: IInArchive = _
  //private var towardsArchive: IOutUpdateArchive[TItem] = _
  private var dst: ISequentialOutStream = _

  def from(src: IInStream): E = {
    this.source = src
    // down-cast to actual ArchiveUpdater in order to
    // enable chain methods calling on concrete ArchiveUpdater.
    this.asInstanceOf[E]
  }

  def towards(dst: ISequentialOutStream): E = {
    this.dst = dst
    this.asInstanceOf[E]
  }

  def update(f: TEntry => TEntry): E = withArchive { (entryNum, archive) =>
    archive.updateItems(dst, entryNum, new DefaultIOutCreateCallback {
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

  def withArchive[R](f: (Int, IOutUpdateArchive[TItem]) => R): R = {
    val from = SevenZip.openInArchive(format, source)
    val to = from.getConnectedOutArchive
    val res = f(from.getNumberOfItems, to.asInstanceOf[IOutUpdateArchive[TItem]])
    from.close()
    res
  }

  def removeWhere(pred: TEntry => Boolean): E = withArchive { (entryNum, archive) =>
    archive.updateItems(dst, entryNum, new DefaultIOutCreateCallback {
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

      override def getStream(i: Int): ISequentialInStream = null
    })
    this.asInstanceOf[E]
  }

  protected def adaptItemToEntry(item: TItem): TEntry

  //def append(entries: Seq[TEntry]): E = {
  //  towardsArchive.updateItems(dst,)
  //  this.asInstanceOf[E]
  //}

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem

  trait DefaultIOutCreateCallback extends IOutCreateCallback[TItem] {
    override def setOperationResult(b: Boolean): Unit = {}

    override def setTotal(l: Long): Unit = {}

    override def setCompleted(l: Long): Unit = {}
  }

}

