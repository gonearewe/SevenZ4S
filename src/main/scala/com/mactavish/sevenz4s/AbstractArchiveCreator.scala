package com.mactavish.compress_sharp.lib

import java.io.RandomAccessFile

import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchive, IOutCreateCallback, IOutItemAllFormats, ISequentialInStream, ISequentialOutStream, SevenZip}
import net.sf.sevenzipjbinding.impl.{OutItemFactory, RandomAccessFileOutStream}

trait AbstractArchiveCreator[E<:AbstractArchiveCreator[_,_],T <: CompressionEntry] {
  private var dst: ISequentialOutStream = _
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
  protected val archivePrototype: IOutCreateArchive[_>:IOutItemAllFormats] =
    SevenZip.openOutArchive(format: ArchiveFormat)

  def towards(dst: ISequentialOutStream): E = {
    //if(dst==null) throw SevenZ4SException("dst has already been set")
    this.dst = dst
    this.asInstanceOf[E]
  }

  def towards(dst: RandomAccessFile): E = towards(new RandomAccessFileOutStream(dst))

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

  def onTabulation(numEntry: Int)(f: => Seq[T]): E = {
    //if(entries==null) throw SevenZ4SException("onTabulation callback function has already been set")
    this.entries = new EntryProxy(f)
    this.numEntry = numEntry
    this.asInstanceOf[E]
  }

  def compress(): Unit = {
    checkAndCompleteConfig()

    // print trace for debugging
    //archivePrototype.setTrace(true)

    archivePrototype.createArchive(dst, numEntry, new IOutCreateCallback[IOutItemAllFormats] {
      private var total: Long = -1

      override def setTotal(l: Long): Unit = this.total = l

      override def setCompleted(l: Long): Unit = if (this.total != -1) onProcess(l, this.total)

      override def setOperationResult(b: Boolean): Unit = onEachEnd(b)

      override def getItemInformation(
                                       i: Int,
                                       outItemFactory: OutItemFactory[IOutItemAllFormats]
                                     ): IOutItemAllFormats = {
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
    })

    this.entries = null // release inner resources
  }

  private def checkAndCompleteConfig(): Unit = {
    if (onProcess == null) onProcess = (_, _) => {}
    if (onEachEnd == null) onEachEnd = _ => {}
    if (entries == null)
      throw SevenZ4SException("creator may not be reused or onTabulation callback function mustn't be empty")
    if (dst == null) throw SevenZ4SException("dst stream mustn't be empty")
  }

  private class EntryProxy(producer: => Seq[T]) {
    lazy val handle: Seq[T] = producer
    private var remains: Seq[T] = _

    def next(): Option[T] = {
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

  private def adaptEntryToItem(entry: CompressionEntry, template: IOutItemAllFormats): IOutItemAllFormats = {
    template.setDataSize(entry.dataSize)
    entry match {
      case gzip: CompressionEntryGZip =>
        template.setPropertyPath(gzip.path)
        template.setPropertyLastModificationTime(gzip.lastModificationTime)
      case bzip2: CompressionEntryBZip2 =>
      case zip: CompressionEntryZip =>
        template.setPropertyPath(zip.path)
        template.setPropertyIsDir(zip.isDir)
        template.setPropertyLastModificationTime(zip.lastModificationTime)
        template.setPropertyLastAccessTime(zip.lastAccessTime)
        template.setPropertyCreationTime(zip.creationTime)
      case sevenz: CompressionEntry7Z =>
        template.setPropertyPath(sevenz.path)
        template.setPropertyIsDir(sevenz.isDir)
        template.setPropertyLastModificationTime(sevenz.lastModificationTime)
        template.setPropertyIsAnti(sevenz.isAnti)
      case _ =>
    }
    template
  }
}

trait ProgressTracker extends ((Long, Long) => Unit) {
  def apply(completed: Long, total: Long): Unit
}
