package com.mactavish.sevenz4s

import java.io.{IOException, RandomAccessFile}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}
import java.util.Calendar

import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream

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
          source = new RandomAccessFileInStream(new RandomAccessFile(file.toFile, "r")),
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
      source = new RandomAccessFileInStream(new RandomAccessFile(f.toFile, "r"))
    )
  }
}
