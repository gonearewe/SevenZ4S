package com.mactavish.sevenz4s

import java.io.InputStream
import java.nio.file.Path
import java.util.{Calendar, Date}


sealed abstract class CompressionEntry(val dataSize: Long, val source: Either[Path, InputStream])

final case class CompressionEntryGZip(
                                       override val dataSize: Long,
                                       override val source: Either[Path, InputStream],
                                       path: String,
                                       lastModificationTime: Date = Calendar.getInstance().getTime
                                     ) extends CompressionEntry(dataSize, source) {
}

final case class CompressionEntryBZip2(
                                        override val dataSize: Long,
                                        override val source: Either[Path, InputStream]
                                      ) extends CompressionEntry(dataSize, source)

final case class CompressionEntryZip(
                                      override val dataSize: Long,
                                      override val source: Either[Path, InputStream],
                                      path: String,
                                      isDir: Boolean,
                                      lastModificationTime: Date = Calendar.getInstance().getTime,
                                      lastAccessTime: Date = Calendar.getInstance().getTime,
                                      creationTime: Date = Calendar.getInstance().getTime,
                                    ) extends CompressionEntry(dataSize, source)


final case class CompressionEntry7Z(
                                     override val dataSize: Long,
                                     override val source: Either[Path, InputStream],
                                     path: String,
                                     isDir: Boolean,
                                     lastModificationTime: Date = Calendar.getInstance().getTime,
                                     //  If true delete corresponding file or directory during extraction.
                                     isAnti: Boolean = false
                                   ) extends CompressionEntry(dataSize, source)

final case class CompressionEntryTar(
                                      override val dataSize: Long,
                                      override val source: Either[Path, InputStream],
                                      path: String,
                                      isDir: Boolean,
                                      lastModificationTime: Date = Calendar.getInstance().getTime,
                                      user: String = "",
                                      group: String = "",
                                      symLink: String = null,
                                      hardLink: String = null
                                    ) extends CompressionEntry(dataSize, source)
