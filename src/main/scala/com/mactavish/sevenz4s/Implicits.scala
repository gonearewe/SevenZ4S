package com.mactavish.sevenz4s

import java.io.RandomAccessFile

import net.sf.sevenzipjbinding.IInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.util.ByteArrayStream

object Implicits {
  implicit def file2IInStream(f: RandomAccessFile): IInStream = new RandomAccessFileInStream(f)

  /**
   * array2IInStream produces an IInStream directly from given array without stream length limit.
   */
  implicit def array2IInStream(a: Array[Byte]): IInStream = new ByteArrayStream(a, false, Int.MaxValue)
}
