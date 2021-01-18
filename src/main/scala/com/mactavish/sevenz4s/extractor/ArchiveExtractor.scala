package com.mactavish.sevenz4s.extractor

import java.io.{Closeable, InputStream, RandomAccessFile}
import java.nio.file.Path

import com.mactavish.sevenz4s.{ExtractionEntry, ProgressTracker, SevenZ4S, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem

import scala.collection.mutable

final class ArchiveExtractor extends Closeable {
  private var source: IInStream = _
  private var archive: IInArchive = _
  private var onProcess: ProgressTracker = ProgressTracker.empty
  private var onEachEnd: ExtractOperationResult => Unit = _ => {}
  private var password: String = _
  private var isClosed = false

  def from(src: Either[Path, InputStream]): ArchiveExtractor = {
    if (src == null) throw SevenZ4SException("`from` doesn't accept null parameter")
    this.source = SevenZ4S.open(src)
    this.archive = SevenZip.openInArchive(null, this.source)
    this
  }

  def onProcess(f: ProgressTracker): ArchiveExtractor = {
    if (f == null) throw SevenZ4SException("`onProcess` doesn't accept null callback")
    this.onProcess = f
    this
  }

  def onEachEnd(f: ExtractOperationResult => Unit): ArchiveExtractor = {
    if (f == null) throw SevenZ4SException("`onEachEnd` doesn't accept null callback")
    this.onEachEnd = f
    this
  }

  def withPassword(passwd: String): ArchiveExtractor = {
    if (passwd == null) throw SevenZ4SException("null passwd is meaningless")
    this.password = passwd
    this
  }

  def foreach(f: ExtractionEntry => Unit): ArchiveExtractor = {
    checkArchive()
    archive.getSimpleInterface.getArchiveItems.foreach(
      item => f(adaptItemToEntry(item))
    )
    this
  }

  def extractTo(dst: Path): ArchiveExtractor = {
    checkArchive()
    val filesToClose = mutable.HashSet[Closeable]()

    archive.extract(
      Array.tabulate(numberOfItems)(identity), false, new IArchiveExtractCallback with ICryptoGetTextPassword {
        private var total: Long = -1

        override def getStream(index: Int, extractAskMode: ExtractAskMode): ISequentialOutStream = {
          // formats that only supports single item, they usually lack `isDir` and `path` property
          val singleArchiveFormats = Set(ArchiveFormat.BZIP2, ArchiveFormat.GZIP)
          if (singleArchiveFormats contains archive.getArchiveFormat) {
            if (extractAskMode == ExtractAskMode.EXTRACT) {
              val f = new RandomAccessFile(dst.toFile, "rw")
              filesToClose.add(f)
              return new RandomAccessFileOutStream(f)
            } else return null
          }

          // for generic api, we can only access item property in this way
          val isDir = archive.getProperty(index, PropID.IS_FOLDER).asInstanceOf[Boolean]
          val pathS = archive.getProperty(index, PropID.PATH).asInstanceOf[String]
          val path = dst.resolve(pathS).toFile
          if (isDir) {
            path.mkdirs()
            null
          } else {
            val f = new RandomAccessFile(path, "rw")
            filesToClose.add(f)
            new RandomAccessFileOutStream(f)
          }
        }

        override def cryptoGetTextPassword(): String = password

        override def prepareOperation(extractAskMode: ExtractAskMode): Unit = {}

        override def setOperationResult(res: ExtractOperationResult): Unit = onEachEnd(res)

        override def setTotal(l: Long): Unit = this.total = l

        override def setCompleted(l: Long): Unit = if (this.total != -1) onProcess(l, this.total)
      })

    filesToClose.foreach(_.close())
    this
  }

  def numberOfItems: Int = {
    checkArchive()
    this.archive.getNumberOfItems
  }

  private def adaptItemToEntry(item: ISimpleInArchiveItem): ExtractionEntry = {
    implicit def optionWrapper[T](x: T): Option[T] = Option(x)

    ExtractionEntry(
      item = item,
      passwd = this.password,
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

  private def checkArchive(): Unit = {
    if (archive == null || source == null)
      throw SevenZ4SException("`archive` or `source` is not set, try to call `from` first")
    if (isClosed)
      throw SevenZ4SException("Extractor is already closed and can't be used anymore")
  }

  def close(): Unit = {
    source.close()
    archive.close()
    isClosed = true
  }
}
