package fun.mactavish.sevenz4s.extractor

import java.io.{Closeable, InputStream, OutputStream, RandomAccessFile}
import java.nio.file.Path

import fun.mactavish.sevenz4s.{ExtractionEntry, SevenZ4S, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem

import scala.collection.mutable

/**
 * Common archive extractor for all kinds of archive formats,
 * it uses format auto-detection insides and provides generic access
 * to archive entries. Unlike `Creator` or `Updater`, it requires
 * user manually closing since it leaks streams out to user through entries,
 * and have no ideas whether user is still using the streams.
 */
final class ArchiveExtractor extends Closeable {
  /**
   * Formats that only supports single item
   */
  private val singleArchiveFormats = Set(ArchiveFormat.BZIP2, ArchiveFormat.GZIP)

  private var source: IInStream = _
  private var archive: IInArchive = _
  private var onProcess: (Long, Long) => Unit = (_, _) => {}
  private var onEachEnd: ExtractOperationResult => Unit = _ => {}
  /**
   * If password is set, then the archive will be opened with given password.
   */
  private var password: String = _
  /**
   * After close, the extractor can't be reused.
   */
  private var isClosed = false

  /**
   * Specifies where archive stream comes from.
   *
   * For `Path`, it reads the given file and closes it eventually after extraction.
   * For `InputStream`, it reads all bytes from the input stream which
   * costs O(N) both on space and time. Therefore, it's usually more
   * efficient to pass `Path` when you intend to provide data from a file.
   *
   * Additionally, in the second case, remember to close `InputStream`
   * on your own afterwards.
   *
   * @param src where archive stream comes from
   * @return extractor itself so that method calls can be chained
   */
  def from(src: Either[Path, InputStream]): ArchiveExtractor = {
    if (src == null) throw SevenZ4SException("`from` doesn't accept null parameter")
    this.source = SevenZ4S.open(src)
    this.archive = SevenZip.openInArchive(null, this.source)
    this
  }

  /**
   * Provides callback triggered during extraction operation resulting from
   * this extractor's `extractTo` method, while `foreach` and entry's independent
   * `extractTo` method doesn't.
   *
   * First parameter of the callback is the number of bytes completed extraction,
   * the second one is the total number of bytes to extract. Together,
   * they can describe the progress of extraction.
   *
   * Note that the time and frequency of this callback's trigger is unknown.
   * And it's only relevant with the bytes to extract, nothing to do with
   * the entries.
   *
   * @param progressTracker (completed, total) => `Unit`
   * @return extractor itself so that method calls can be chained
   */
  def onProcess(progressTracker: (Long, Long) => Unit): ArchiveExtractor = {
    if (progressTracker == null) throw SevenZ4SException("`onProcess` doesn't accept null callback")
    this.onProcess = progressTracker
    this
  }

  /**
   * Provides callback triggered after each entry's extraction.
   * And only this extractor's `extractTo` method triggers it,
   * while `foreach` and entry's independent `extractTo` method doesn't.
   *
   * TODO: not guaranteed to be exactly called after each entry's extraction
   *
   * @param f `ExtractOperationResult`(enumeration) => Unit
   * @return extractor itself so that method calls can be chained
   */
  def onEachEnd(f: ExtractOperationResult => Unit): ArchiveExtractor = {
    if (f == null) throw SevenZ4SException("`onEachEnd` doesn't accept null callback")
    this.onEachEnd = f
    this
  }

  /**
   * Provides password for encrypted archive.
   * Note that supplying no password for encrypted archive will result in
   * a silent failure.
   *
   * @param passwd password
   * @return extractor itself so that method calls can be chained
   */
  def withPassword(passwd: String): ArchiveExtractor = {
    if (passwd == null) throw SevenZ4SException("null passwd is meaningless")
    this.password = passwd
    this
  }

  /**
   * Extracts the entire archive to directory represented by `dst`.
   *
   * Note that it's more efficient to use this `extractTo` than call
   * entry's independent `extractTo` within `foreach` if you intend to
   * extract the entire archive. `extractTo` can be called for
   * multiple times.
   *
   * @param dst directory to extract to
   * @return extractor itself so that method calls can be chained
   */
  def extractTo(dst: Path): ArchiveExtractor = {
    checkArchive()
    val filesToClose = mutable.HashSet[Closeable]()

    archive.extract(
      Array.tabulate(numberOfEntries)(identity), false, new IArchiveExtractCallback with ICryptoGetTextPassword {
        private var total: Long = -1

        override def getStream(index: Int, extractAskMode: ExtractAskMode): ISequentialOutStream = {
          // formats that only supports single item usually lack `isDir` and `path` property
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

  /**
   * Gets the number of entries in this archive.
   *
   * @return the number of entries
   */
  def numberOfEntries: Int = {
    checkArchive()
    this.archive.getNumberOfItems
  }

  /**
   * If and only if this archive's format only supports single entry compression,
   * this method extracts the very single entry to an `OutputStream`.
   * `extractTo` can be called for multiple times.
   *
   * @param dst `OutputStream` to extract to
   * @return extractor itself so that method calls can be chained
   */
  def extractTo(dst: OutputStream): ArchiveExtractor = {
    checkArchive()
    if (!singleArchiveFormats.contains(this.archiveFormat))
      throw SevenZ4SException(s"${this.archiveFormat} may contain multiple items, thus doesn't support this method")

    this.foreach(e => e.extractTo(Right(dst))) // `foreach` called only on one item
  }

  /**
   * Do some thing on each archive entry. You can gain access to each entry's
   * properties and extract them independently. `foreach` can be called for
   * multiple times.
   *
   * @param f function to process each entry
   * @return extractor itself so that method calls can be chained
   */
  def foreach(f: ExtractionEntry => Unit): ArchiveExtractor = {
    checkArchive()
    archive.getSimpleInterface.getArchiveItems.foreach(
      item => f(adaptItemToEntry(item))
    )
    this
  }

  /**
   * Gets the format of this archive.
   *
   * @return the format of this archive
   */
  def archiveFormat: ArchiveFormat = {
    checkArchive()
    this.archive.getArchiveFormat
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

  /**
   * Closes this extractor and releases resources. After this operation,
   * `extractTo` and `foreach` can't be called anymore, and it's also
   * not safe to use `ExtractionEntry` acquired from `foreach` anymore.
   * In a word, close destroys this extractor.
   */
  def close(): Unit = {
    source.close()
    archive.close()
    isClosed = true
  }
}
