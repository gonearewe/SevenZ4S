package com.mactavish.compress_sharp.lib

import java.io.RandomAccessFile

import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchive, IOutCreateArchive7z, IOutCreateArchiveGZip, IOutCreateArchiveZip, IOutFeatureSetEncryptHeader, IOutFeatureSetLevel, IOutFeatureSetMultithreading, IOutFeatureSetSolid, IOutItemAllFormats, IOutItemZip, ISequentialOutStream}
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream

class ArchiveCreatorZip() extends {
  /**
   * Use `early definition` syntax to make sure format get initialized before super trait.
   */
  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
} with AbstractArchiveCreator[ArchiveCreatorZip, CompressionEntryZip] {
  private val archive = archivePrototype.asInstanceOf[IOutCreateArchiveZip]

  def setLevel(compressionLevel: Int): ArchiveCreatorZip = {
    archive.setLevel(compressionLevel)
    this
  }
}

class ArchiveCreatorGZip() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
} with AbstractArchiveCreator[ArchiveCreatorGZip, CompressionEntryGZip] {
  private val archive = archivePrototype.asInstanceOf[IOutCreateArchiveGZip]

  def setLevel(compressionLevel: Int): ArchiveCreatorGZip = {
    archive.setLevel(compressionLevel)
    this
  }
}

class ArchiveCreator7Z() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
} with AbstractArchiveCreator[ArchiveCreator7Z, CompressionEntry7Z] {
  private val archive = archivePrototype.asInstanceOf[IOutCreateArchive7z]

  def setLevel(compressionLevel: Int): ArchiveCreator7Z = {
    archive.setLevel(compressionLevel)
    this
  }

  def setSolid(b: Boolean): ArchiveCreator7Z = {
    archive.setSolid(b)
    this
  }

  def setSolidFiles(i: Int): ArchiveCreator7Z = {
    archive.setSolidFiles(i)
    this
  }

  def setSolidSize(l: Long): ArchiveCreator7Z = {
    archive.setSolidSize(l)
    this
  }

  def setSolidExtension(b: Boolean): ArchiveCreator7Z = {
    archive.setSolidExtension(b)
    this
  }

  def setThreadCount(threadCount: Int): ArchiveCreator7Z = {
    archive.setThreadCount(threadCount)
    this
  }

  def setHeaderEncryption(b: Boolean): ArchiveCreator7Z = {
    archive.setHeaderEncryption(b)
    this
  }
}
