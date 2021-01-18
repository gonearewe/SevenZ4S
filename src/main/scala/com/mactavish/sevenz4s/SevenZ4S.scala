package com.mactavish.sevenz4s

import java.io._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}
import java.util.Calendar

import net.sf.sevenzipjbinding.impl.{RandomAccessFileInStream, RandomAccessFileOutStream}
import net.sf.sevenzipjbinding.util.ByteArrayStream
import net.sf.sevenzipjbinding.{IInStream, IOutStream}

import scala.collection.mutable

object SevenZ4S {
  def get7ZEntriesFrom(root: Path): Seq[CompressionEntry7Z] = {
    val entries = mutable.ArrayBuffer[CompressionEntry7Z]()

    Files.walkFileTree(root, new FileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        entries.append(CompressionEntry7Z(
          dataSize = 0,
          source = null,
          path = root.getParent.relativize(dir).toString,
          isDir = true,
          lastModificationTime = Calendar.getInstance().getTime
        ))
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        entries.append(CompressionEntry7Z(
          dataSize = Files.size(file),
          source = Left(file),
          path = root.getParent.relativize(file).toString,
          isDir = false,
          lastModificationTime = Calendar.getInstance().getTime
        ))
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

  def getBZip2EntryFrom(f: Path): CompressionEntryBZip2 = {
    CompressionEntryBZip2(
      dataSize = Files.size(f),
      source = Left(f)
    )
  }

  private[sevenz4s] def open(in: Either[Path, InputStream]): IInStream = {
    in match {
      case Left(f) =>
        // in-stream requires `RandomAccessFile` opening in "r" mode
        new RandomAccessFileInStream(new RandomAccessFile(f.toFile, "r"))
      case Right(s) =>
        // ByteArrayStream without max length limit
        val in = new ByteArrayStream(Int.MaxValue)
        in.writeFromInputStream(s, false)
        in
    }
  }

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
        // ByteArrayStream without max length limit
        val out = new ByteArrayStream(Int.MaxValue)
        out.writeToOutputStream(s, false)
        out
    }
  }
}
