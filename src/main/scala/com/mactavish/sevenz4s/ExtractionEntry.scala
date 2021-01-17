package com.mactavish.sevenz4s

import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.Date

import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem
import net.sf.sevenzipjbinding.util.ByteArrayStream

final case class ExtractionEntry(
                                  private val item: ISimpleInArchiveItem,
                                  private val passwd: String,
                                  originalSize: Long,
                                  packedSize: Long,
                                  path: String,
                                  isDir: Boolean,
                                  compressMethod: String,
                                  lastAccessTime: Option[Date] = None,
                                  creationTime: Option[Date] = None,
                                  isEncrypted: Boolean = false,
                                  user: String = "",
                                  group: String = "",
                                  hostOS: String = "",
                                  comment: String = "",
                                  CRC: Int) {

  def extractTo(dst: Path): ExtractOperationResult = {
    val f = new RandomAccessFile(dst.toFile, "rw")
    if (passwd == null)
      item.extractSlow(new RandomAccessFileOutStream(f))
    else
      item.extractSlow(new RandomAccessFileOutStream(f), passwd)
  }

  def extractTo(dst: Array[Byte]): ExtractOperationResult = {
    val s = new ByteArrayStream(dst, false, Int.MaxValue)
    if (passwd == null)
      item.extractSlow(s)
    else
      item.extractSlow(s, passwd)
  }
}
