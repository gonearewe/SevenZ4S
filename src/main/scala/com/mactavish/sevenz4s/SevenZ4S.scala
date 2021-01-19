package com.mactavish.sevenz4s

import java.io._
import java.nio.file.attribute.{BasicFileAttributes, FileOwnerAttributeView, PosixFileAttributes}
import java.nio.file._
import java.util.{Calendar, Date}

import net.sf.sevenzipjbinding.impl.{RandomAccessFileInStream, RandomAccessFileOutStream}
import net.sf.sevenzipjbinding.util.ByteArrayStream
import net.sf.sevenzipjbinding.{IInStream, IOutStream}

import scala.collection.mutable

/**
 * Provides some util functions.
 */
object SevenZ4S {
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
    // TODO: I haven't tested these two properties, be careful
    val user = Files.getFileAttributeView(f, classOf[FileOwnerAttributeView]).getOwner.getName
    val group = Files.readAttributes(f, classOf[PosixFileAttributes], LinkOption.NOFOLLOW_LINKS).group.getName
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
   * Util to open `Path` or `InputStream`.
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
   * Util to open `Path` or `OutputStream`.
   *
   * For `Path`, it creates a `RandomAccessFileOutStream`,
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
