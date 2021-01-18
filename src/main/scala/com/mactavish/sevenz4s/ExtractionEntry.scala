package com.mactavish.sevenz4s

import java.io.OutputStream
import java.nio.file.Path
import java.util.Date

import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem

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

  def extractTo(dst: Either[Path, OutputStream]): ExtractOperationResult = {
    val s = SevenZ4S.open(dst)
    val res = if (passwd == null)
      item.extractSlow(s)
    else
      item.extractSlow(s, passwd)
    s.close()
    res
  }
}
