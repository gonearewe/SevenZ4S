package fun.mactavish.sevenz4s

import java.io._
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, FileOwnerAttributeView}
import java.util.{Calendar, Date}

import fun.mactavish.sevenz4s.creator._
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
import net.sf.sevenzipjbinding.impl.{RandomAccessFileInStream, RandomAccessFileOutStream}
import net.sf.sevenzipjbinding.util.ByteArrayStream
import net.sf.sevenzipjbinding.{IInStream, IOutStream}

import scala.collection.mutable

/**
 * Provides some utility functions.
 */
object SevenZ4S {
  /**
   * Useful utility function to compress archive on local file system.
   *
   * @param format specifies the format of wanted archive
   * @param from   path of the directory or the file to be compressed,
   *               `GZip` and `BZip2` format can only compress single file
   * @param to     path of directory where the generated archive to be put into, the name
   *               of the archive will be `from`'s base name plus format-specific extension name,
   *               if `to` doesn't exist yet, it will be automatically created
   */
  def compress(format: CreatableArchiveFormat, from: Path, to: Path): Unit = {
    format match {
      case ArchiveFormat.SEVEN_Z =>
        val name = to.resolve(from.getFileName + ".7z")
        new ArchiveCreator7Z().towards(Left(name)).compress(get7ZEntriesFrom(from))
      case ArchiveFormat.ZIP =>
        val name = to.resolve(from.getFileName + ".zip")
        new ArchiveCreatorZip().towards(Left(name)).compress(getZipEntriesFrom(from))
      case ArchiveFormat.TAR =>
        val name = to.resolve(from.getFileName + ".tar")
        new ArchiveCreatorTar().towards(Left(name)).compress(getTarEntriesFrom(from))
      case ArchiveFormat.GZIP =>
        val name = to.resolve(from.getFileName + ".gz")
        new ArchiveCreatorGZip().towards(Left(name)).compress(getGZipEntryFrom(from))
      case ArchiveFormat.BZIP2 =>
        val name = to.resolve(from.getFileName + ".bz2")
        new ArchiveCreatorBZip2().towards(Left(name)).compress(getBZip2EntryFrom(from))
    }
  }

  /**
   * Useful utility function to extract archive on local file system.
   *
   * @param from path of the archive file
   * @param to   path of directory where the archive's content extracts into,
   *             if `to` doesn't exist yet, it will be automatically created
   */
  def extract(from: Path, to: Path): Unit = {
    val dst = if (formatOf(Left(from)).isInstanceOf[SingleArchiveFormat]) {
      // trip the file extension
      val name = from.toFile.getName.split('.').init.mkString(".")
      // the path of the single extraction file
      to.resolve(name).toFile.toPath
    } else to

    new ArchiveExtractor()
      .from(Left(from))
      .extractTo(dst)
      .close()
  }

  /**
   * Gets the format of given archive.
   *
   * If you already obtain an instance of `ArchiveExtractor`, use
   * its `archiveFormat` method instead.
   *
   * @param archive archive source
   * @return the enumeration of `ArchiveFormat`
   */
  def formatOf(archive: Either[Path, InputStream]): ArchiveFormat = {
    val extractor = new ArchiveExtractor().from(archive)
    val ans = extractor.archiveFormat
    extractor.close()
    ans
  }

  /**
   * Generates a `CompressionEntry7Z` from given file or directory path.
   *
   * @param root a file or directory path
   * @return archive entries representing given file(s), with order in `Seq` unknown.
   */
  def get7ZEntriesFrom(root: Path): Seq[CompressionEntry7Z] =
    getEntriesFrom(root, classOf[CompressionEntry7Z])

  /**
   * Generates a `CompressionEntryZip` from given file or directory path.
   *
   * @param root a file or directory path
   * @return archive entries representing given file(s), with order in `Seq` unknown.
   */
  def getZipEntriesFrom(root: Path): Seq[CompressionEntryZip] =
    getEntriesFrom(root, classOf[CompressionEntryZip])

  /**
   * Generates a `CompressionEntryTar` from given file or directory path.
   *
   * @param root a file or directory path
   * @return archive entries representing given file(s), with order in `Seq` unknown.
   */
  def getTarEntriesFrom(root: Path): Seq[CompressionEntryTar] =
    getEntriesFrom(root, classOf[CompressionEntryTar])

  private def getEntriesFrom[T <: CompressionEntry](root: Path, typ: Class[T]): Seq[T] = {
    val entries = mutable.ArrayBuffer[T]()

    Files.walkFileTree(root, new FileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        typ match {
          case x if x == classOf[CompressionEntry7Z] =>
            entries.append(get7ZEntryFrom(dir, root.getParent).asInstanceOf[T])
          case x if x == classOf[CompressionEntryZip] =>
            entries.append(getZipEntryFrom(dir, root.getParent).asInstanceOf[T])
          case x if x == classOf[CompressionEntryTar] =>
            entries.append(getTarEntryFrom(dir, root.getParent).asInstanceOf[T])
        }
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        typ match {
          case x if x == classOf[CompressionEntry7Z] =>
            entries.append(get7ZEntryFrom(file, root.getParent).asInstanceOf[T])
          case x if x == classOf[CompressionEntryZip] =>
            entries.append(getZipEntryFrom(file, root.getParent).asInstanceOf[T])
          case x if x == classOf[CompressionEntryTar] =>
            entries.append(getTarEntryFrom(file, root.getParent).asInstanceOf[T])
        }
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = throw exc

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        if (exc != null) throw exc
        else FileVisitResult.CONTINUE
      }
    })

    entries.toSeq
  }

  private def get7ZEntryFrom(f: Path, root: Path): CompressionEntry7Z = {
    if (f.toFile.isDirectory) {
      CompressionEntry7Z(
        dataSize = 0,
        source = null,
        path = root.relativize(f).toString,
        isDir = true,
        lastModificationTime = now
      )
    } else {
      CompressionEntry7Z(
        dataSize = Files.size(f),
        source = Left(f),
        path = root.relativize(f).toString,
        isDir = false,
        lastModificationTime = now
      )
    }
  }

  private def now: Date = Calendar.getInstance().getTime

  private def getZipEntryFrom(f: Path, root: Path): CompressionEntryZip = {
    if (f.toFile.isDirectory) {
      CompressionEntryZip(
        dataSize = 0,
        source = null,
        path = root.relativize(f).toString,
        isDir = true,
        lastModificationTime = now,
        lastAccessTime = now,
        creationTime = now
      )
    } else {
      CompressionEntryZip(
        dataSize = Files.size(f),
        source = Left(f),
        path = root.relativize(f).toString,
        isDir = false,
        lastModificationTime = now,
        lastAccessTime = now,
        creationTime = now
      )
    }
  }

  private def getTarEntryFrom(f: Path, root: Path): CompressionEntryTar = {
    val user = Files.getFileAttributeView(f, classOf[FileOwnerAttributeView]).getOwner.getName
    // TODO: group access fails, I don't know how to get this property
    //val group = Files.readAttributes(f, classOf[PosixFileAttributes], LinkOption.NOFOLLOW_LINKS).group.getName
    val group = ""
    if (f.toFile.isDirectory) {
      CompressionEntryTar(
        dataSize = 0,
        source = null,
        path = root.relativize(f).toString,
        isDir = true,
        lastModificationTime = now,
        user = user,
        group = group,
        symLink = null,
        hardLink = null
      )
    } else {
      CompressionEntryTar(
        dataSize = Files.size(f),
        source = Left(f),
        path = root.relativize(f).toString,
        isDir = false,
        lastModificationTime = now,
        user = user,
        group = group,
        // TODO: I don't know what these two properties should be yet
        symLink = null,
        hardLink = null
      )
    }
  }

  /**
   * Generates a `CompressionEntryBZip2` from given file path.
   *
   * @param f a file path
   * @return archive entry representing given file
   */
  def getBZip2EntryFrom(f: Path): CompressionEntryBZip2 = {
    CompressionEntryBZip2(
      dataSize = Files.size(f),
      source = Left(f)
    )
  }

  /**
   * Generates a `CompressionEntryGZip` from given file path.
   *
   * @param f a file path
   * @return archive entry representing given file
   */
  def getGZipEntryFrom(f: Path): CompressionEntryGZip = {
    CompressionEntryGZip(
      dataSize = Files.size(f),
      source = Left(f),
      path = f.getFileName.toString,
      lastModificationTime = now
    )
  }

  /**
   * Utility to open `Path` or `InputStream`.
   *
   * For `Path`, it creates a `RandomAccessFileInStream`,
   * and for `InputStream`, it creates `ByteArrayStream` and
   * reads all bytes from the input stream which
   * costs O(N) both on space and time. Therefore, it's usually more
   * efficient to pass `Path` when you intend to provide data from a file.
   *
   * Additionally, in the second case, remember to close `InputStream`
   * on your own afterwards.
   *
   * @param in where stream comes from
   * @return an instance of `IInStream`
   */
  private[sevenz4s] def open(in: Either[Path, InputStream]): IInStream = {
    in match {
      case Left(f) =>
        // in-stream requires `RandomAccessFile` opening in "r" mode
        new RandomAccessFileInStream(new RandomAccessFile(f.toFile, "r"))
      case Right(s) =>
        // `ByteArrayStream` without max length limit
        val in = new ByteArrayStream(Int.MaxValue)
        in.writeFromInputStream(s, false)
        // NOTICE: must rewind to reset cursor to zero so that can be read afterwards
        in.rewind()
        in
    }
  }

  /**
   * Utility to open `Path` or `OutputStream`.
   *
   * For `Path`, it creates a `RandomAccessFileOutStream` and possibly non-existed
   * parent directory,
   * and for `OutputStream`, it creates `ByteArrayStream` and promises to
   * flush data into given `OutputStream` on close.
   *
   * Additionally, in the second case, remember to close `OutputStream`
   * on your own afterwards.
   *
   * @param in where stream goes into
   * @return an instance of `IOutStream` which can be closed
   */
  private[sevenz4s] def open(in: Either[Path, OutputStream]): IOutStream with Closeable = {
    in match {
      case Left(f) =>
        if (!Files.exists(f.getParent))
          f.getParent.toFile.mkdirs()
        // out-stream requires `RandomAccessFile` opening in "rw" mode
        val file = new RandomAccessFile(f.toFile, "rw")
        new RandomAccessFileOutStream(file) with Closeable {
          override def close(): Unit = {
            file.close()
          }
        }
      case Right(s) =>
        // `ByteArrayStream` without max length limit
        new ByteArrayStream(Int.MaxValue) {
          // `ByteArrayStream`'s `close` method isn't meaningless now,
          // must ensure that it's closed after usage, otherwise user will get nothing
          override def close(): Unit = {
            // attach user's OutputStream here, where data will be written on close
            writeToOutputStream(s, false)
            super.close()
          }
        }
    }
  }
}
