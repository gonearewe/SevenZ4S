package com.mactavish.sevenz4s.creator

import java.io.RandomAccessFile

import com.mactavish.sevenz4s.{CompressionEntry, ProgressTracker, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.{OutItemFactory, RandomAccessFileOutStream}

trait AbstractArchiveCreator[E <: AbstractArchiveCreator[E]] {
  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  private var dst: ISequentialOutStream = _
  private var password: String = _
  private var onProcess: ProgressTracker = _
  private var onEachEnd: Boolean => Unit = _
  private var entries: EntryProxy = _
  private var numEntry: Int = _
  /**
   * Subclass override `format` to specify which format of archive it
   * intends to create.
   *
   * Subclass must make sure format get initialized before this trait,
   * (by early definition, maybe) as archivePrototype will use it during
   * trait initialization.
   */
  protected val format: ArchiveFormat
  protected val archivePrototype: IOutCreateArchive[TItem] =
    SevenZip.openOutArchive(format: ArchiveFormat).asInstanceOf[IOutCreateArchive[TItem]]

  def onTabulation(numEntry: Int)(f: => Seq[TEntry]): E = {
    //if(entries==null) throw SevenZ4SException("onTabulation callback function has already been set")
    this.entries = new EntryProxy(f)
    this.numEntry = numEntry
    this.asInstanceOf[E]
  }

  def compress(): Unit = {
    checkAndCompleteConfig()

    // print trace for debugging
    //archivePrototype.setTrace(true)

    archivePrototype.createArchive(dst, numEntry, new IOutCreateCallback[TItem] with ICryptoGetTextPassword {
      private var total: Long = -1

      override def setTotal(l: Long): Unit = this.total = l

      override def setCompleted(l: Long): Unit = if (this.total != -1) onProcess(l, this.total)

      override def setOperationResult(b: Boolean): Unit = onEachEnd(b)

      override def getItemInformation(
                                       i: Int,
                                       outItemFactory: OutItemFactory[TItem]
                                     ): TItem = {
        val templateItem = outItemFactory.createOutItem()
        if (i == 0) entries.reset()
        entries.next() match {
          case Some(entry) => adaptEntryToItem(entry, templateItem)
          case None =>
            throw SevenZ4SException(s"onTabulation callback function only provided $i entries, $numEntry expected")
        }
      }

      override def getStream(i: Int): ISequentialInStream = {
        if (i == 0) entries.reset()
        entries.next() match {
          case Some(entry) => entry.source
          case None =>
            // shouldn't happen as `getItemInformation` has checked already
            throw SevenZ4SException(s"onTabulation callback function only provided $i entries, $numEntry expected")
        }
      }

      /**
       * If null is passed, simply means no password, and it won't crash at runtime.
       */
      override def cryptoGetTextPassword(): String = password
    })

    this.entries = null // release inner resources
  }

  def towards(dst: ISequentialOutStream): E = {
    //if(dst==null) throw SevenZ4SException("dst has already been set")
    this.dst = dst
    // down-cast to actual ArchiveCreator in order to
    // enable chain methods calling on concrete ArchiveCreator.
    this.asInstanceOf[E]
  }

  def towards(dst: RandomAccessFile): E = towards(new RandomAccessFileOutStream(dst))

  def setPassword(passwd: String): E = {
    this.password = passwd
    this.asInstanceOf[E]
  }

  def onProcess(f: ProgressTracker): E = {
    //if(onProcess==null) throw SevenZ4SException("onProcess callback function has already been set")
    this.onProcess = f
    this.asInstanceOf[E]
  }

  def onEachEnd(f: Boolean => Unit): E = {
    //if(onEachEnd==null) throw SevenZ4SException("onEachEnd callback function has already been set")
    this.onEachEnd = f
    this.asInstanceOf[E]
  }

  protected def adaptItemToEntry(item: TItem): TEntry

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem

  private def checkAndCompleteConfig(): Unit = {
    if (onProcess == null) onProcess = (_, _) => {}
    if (onEachEnd == null) onEachEnd = _ => {}
    if (entries == null)
      throw SevenZ4SException("creator may not be reused or onTabulation callback function mustn't be empty")
    if (dst == null) throw SevenZ4SException("dst stream mustn't be empty")
  }

  private class EntryProxy(producer: => Seq[TEntry]) {
    lazy val handle: Seq[TEntry] = producer
    private var remains: Seq[TEntry] = _

    def next(): Option[TEntry] = {
      if (remains == null) remains = handle
      if (remains == null) None
      else {
        val a = remains.head
        remains = remains.tail
        Some(a)
      }
    }

    def reset(): Unit = {
      remains = null
    }
  }
}
