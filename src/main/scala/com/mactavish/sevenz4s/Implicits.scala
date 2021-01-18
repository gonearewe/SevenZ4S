package com.mactavish.sevenz4s

import java.io.{InputStream, OutputStream, RandomAccessFile}
import java.nio.file.Path

import net.sf.sevenzipjbinding.IInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.util.ByteArrayStream

object Implicits {
  implicit def file2IInStream(f: Path): IInStream =
    new RandomAccessFileInStream(new RandomAccessFile(f.toFile, "r"))

  /**
   * array2IInStream produces an IInStream directly from given array without stream length limit.
   */
  implicit def array2IInStream(a: Array[Byte]): IInStream = new ByteArrayStream(a, false, Int.MaxValue)

  implicit def pathSourceWrapper(a: Path): Left[Path, Nothing] = Left(a)

  implicit def inStreamSourceWrapper(a: InputStream): Right[Nothing, InputStream] = Right(a)

  implicit def outStreamSourceWrapper(a: OutputStream): Right[Nothing, OutputStream] = Right(a)
}
