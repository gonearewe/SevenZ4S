package com.mactavish.sevenz4s.extractor

import java.io.{File, RandomAccessFile}

import com.mactavish.sevenz4s.{ExtractionEntry, ProgressTracker}
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem
import net.sf.sevenzipjbinding.util.ByteArrayStream
import net.sf.sevenzipjbinding.{ExtractOperationResult, IInArchive, IInStream, SevenZip}

import scala.collection.mutable

final class ArchiveExtractor extends AutoCloseable {
  private val closers = mutable.Seq[AutoCloseable]()
  private var source: IInStream = _
  private var archive: IInArchive = _
  private var onProcess: ProgressTracker = _
  private var onEachEnd: ExtractOperationResult => Unit = _

  def from(a: File): ArchiveExtractor = {
    val f = new RandomAccessFile(a, "r")
    this.closers appended f
    this.source = new RandomAccessFileInStream(f)
    this.archive = SevenZip.openInArchive(null, this.source)
    this.closers appended this.archive
    this
  }

  def from(a: Array[Byte]): ArchiveExtractor = {
    this.source = new ByteArrayStream(a, false, Int.MaxValue)
    this.archive = SevenZip.openInArchive(null, this.source)
    this.closers appended this.archive
    this
  }

  def onProcess(f: ProgressTracker): ArchiveExtractor = {
    //if(onProcess==null) throw SevenZ4SException("onProcess callback function has already been set")
    this.onProcess = f
    this
  }

  def onEachEnd(f: ExtractOperationResult => Unit): ArchiveExtractor = {
    //if(onEachEnd==null) throw SevenZ4SException("onEachEnd callback function has already been set")
    this.onEachEnd = f
    this
  }

  def foreach(f: ExtractionEntry => Unit): ArchiveExtractor = {
    archive.getSimpleInterface.getArchiveItems.foreach(item => f(adaptItemToEntry(item)))
    this
  }

  //def extractTo(dst: Seq[File]): ArchiveExtractor = {
  //  val fs = dst.map(new RandomAccessFile(_, "rw"))
  //  extractTo(fs.map(new RandomAccessFileOutStream(_)))
  //  fs.foreach(_.close())
  //  this
  //}
  //
  //def extractTo(dst: Seq[Array[Byte]]): ArchiveExtractor = {
  //  extractTo(dst.map(new ByteArrayStream(_, false, Int.MaxValue)))
  //  this
  //}
  //
  //private def extractTo(dst: Seq[ISequentialOutStream]): Unit = {
  //  archive.extract(Array.tabulate(numberOfItems)(identity), false, new IArchiveExtractCallback {
  //    private var total: Long = -1
  //
  //    override def getStream(index: Int, extractAskMode: ExtractAskMode): ISequentialOutStream = ???
  //
  //    override def prepareOperation(extractAskMode: ExtractAskMode): Unit = {}
  //
  //    override def setOperationResult(res: ExtractOperationResult): Unit = onEachEnd(res)
  //
  //    override def setTotal(l: Long): Unit = this.total = l
  //
  //    override def setCompleted(l: Long): Unit = if (this.total != -1) onProcess(l, this.total)
  //  })
  //}

  private def adaptItemToEntry(item: ISimpleInArchiveItem): ExtractionEntry = {
    implicit def optionWrapper[T](x: T): Option[T] = Option(x)

    ExtractionEntry(
      item = item,
      originalSize = item.getSize,
      packedSize = item.getPackedSize,
      path = item.getPath,
      isDir = item.isFolder,
      compressMethod = item.getMethod,
      lastAccessTime = item.getLastAccessTime,
      creationTime = item.getCreationTime,
      isEncrypted = item.isEncrypted,
      user = item.getUser,
      group = item.getGroup,
      hostOS = item.getHostOS,
      comment = item.getComment,
      CRC = item.getCRC
    )
  }

  def numberOfItems: Int = this.archive.getNumberOfItems

  def close(): Unit = this.closers.foreach(c => c.close())
}
