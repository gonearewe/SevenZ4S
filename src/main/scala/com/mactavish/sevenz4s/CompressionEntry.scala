package com.mactavish.compress_sharp.lib

import java.lang
import java.util.{Calendar, Date}

import net.sf.sevenzipjbinding._


sealed class CompressionEntry(val dataSize: Long, val source: ISequentialInStream) {

}

final class CompressionEntryGZip(
                                  dataSize: Long,
                                  source: ISequentialInStream,
                                  val path: String,
                                  val lastModificationTime: Date = Calendar.getInstance().getTime
                                ) extends CompressionEntry(dataSize, source) {
}

final class CompressionEntryBZip2(dataSize: Long, source: ISequentialInStream) extends CompressionEntry(dataSize,
  source) {

}

final class CompressionEntryZip(
                                 dataSize: Long,
                                 source: ISequentialInStream,
                                 val path: String,
                                 val isDir: Boolean,
                                 val lastModificationTime: Date = Calendar.getInstance().getTime,
                                 val lastAccessTime: Date= Calendar.getInstance().getTime,
                                 val creationTime: Date = Calendar.getInstance().getTime,
                               ) extends CompressionEntry(dataSize, source) {

}


final class CompressionEntry7Z(
                                dataSize: Long,
                                source: ISequentialInStream,
                                val path: String,
                                val isDir: Boolean,
                                val lastModificationTime: Date= Calendar.getInstance().getTime,
                                val isAnti: Boolean = false //  If true delete corresponding file or directory during
                                // extraction.
                              ) extends CompressionEntry(dataSize, source) {

}
