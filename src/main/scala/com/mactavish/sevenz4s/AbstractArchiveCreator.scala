package com.mactavish.compress_sharp.lib

import java.io.RandomAccessFile

import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateCallback, IOutItemAllFormats, ISequentialInStream, ISequentialOutStream, SevenZip}
import net.sf.sevenzipjbinding.impl.{OutItemFactory, RandomAccessFileOutStream}

trait AbstractArchiveCreator[T <: CompressionEntry] {
  private var dst: ISequentialOutStream = _
  private var onProcess: ProgressTracker = _
  private var onEachEnd: Boolean => Unit = _
  private var entries: EntryProxy = _
  private var numEntry: Int = _
  protected val format: ArchiveFormat

  def towards(dst: ISequentialOutStream): AbstractArchiveCreator[T] = {
    //if(dst==null) throw SevenZ4SException("dst has already been set")
    this.dst = dst
    this
  }

  def towards(dst: RandomAccessFile): AbstractArchiveCreator[T] = towards(new RandomAccessFileOutStream(dst))

  def onProcess(f: ProgressTracker): AbstractArchiveCreator[T] = {
    //if(onProcess==null) throw SevenZ4SException("onProcess callback function has already been set")
    this.onProcess = f
    this
  }

  def onEachEnd(f: Boolean => Unit): AbstractArchiveCreator[T] = {
    //if(onEachEnd==null) throw SevenZ4SException("onEachEnd callback function has already been set")
    this.onEachEnd = f
    this
  }

  def onTabulation(numEntry: Int)(f: => Seq[T]): AbstractArchiveCreator[T] = {
    //if(entries==null) throw SevenZ4SException("onTabulation callback function has already been set")
    this.entries = new EntryProxy(f)
    this.numEntry = numEntry
    this
  }

  def compress(): Unit = {
    checkAndCompleteConfig()
    val archive = SevenZip.openOutArchive(format: ArchiveFormat)
    // print trace for debugging
    //archive.setTrace(true)

    archive.createArchive(dst, numEntry, new IOutCreateCallback[IOutItemAllFormats] {
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
    if (entries == null) throw SevenZ4SException("onTabulation callback function mustn't be empty")
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
