package com.mactavish.sevenz4s

import java.io.OutputStream
import java.nio.file.Path
import java.util.Date

import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem

/**
 * Representation for archive entry used during extraction,
 * it contains specific properties and can extract data to given stream.
 */
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

  /**
   * Extracts its data to the given file or `OutputStream`.
   *
   * We won't close the `OutputStream`, you have to close it on your own.
   *
   * Note that it's more efficient to use extractor's `extractTo` than
   * this entry's independent `extractTo` if you intend to extract the
   * entire archive. `extractTo` can be called for multiple times.
   *
   * @param dst the destination for output archive entry stream
   * @return enumeration of `ExtractOperationResult`
   */
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
