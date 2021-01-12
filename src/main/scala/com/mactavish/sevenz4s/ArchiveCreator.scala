package com.mactavish.compress_sharp.lib

import java.io.RandomAccessFile

import net.sf.sevenzipjbinding.{ArchiveFormat, ISequentialOutStream}
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream

class ArchiveCreatorZip() extends AbstractArchiveCreator[CompressionEntryZip] {
  override protected val format: ArchiveFormat = ArchiveFormat.ZIP
}

class ArchiveCreatorGZip() extends AbstractArchiveCreator[CompressionEntryGZip] {
  override protected val format: ArchiveFormat = ArchiveFormat.GZIP
}

class ArchiveCreator7Z() extends AbstractArchiveCreator[CompressionEntry7Z] {
  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
}
