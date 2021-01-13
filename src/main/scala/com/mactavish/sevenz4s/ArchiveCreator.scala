package com.mactavish.sevenz4s

import com.mactavish.sevenz4s.ArchiveCreatorFeature._
import net.sf.sevenzipjbinding._

final class ArchiveCreatorZip() extends {
  /**
   * Use `early definition` syntax to make sure format get initialized before super trait.
   */
  override protected val format: ArchiveFormat = ArchiveFormat.ZIP
} with AbstractArchiveCreator[ArchiveCreatorZip, CompressionEntryZip]
  with SetLevel[ArchiveCreatorZip] {
  /**
   * Cast and expose `archive` in order to set possible features.
   */
  override protected val archive: IOutCreateArchiveZip = archivePrototype.asInstanceOf[IOutCreateArchiveZip]
}

final class ArchiveCreatorGZip() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.GZIP
} with AbstractArchiveCreator[ArchiveCreatorGZip, CompressionEntryGZip]
  with SetLevel[ArchiveCreatorGZip] {
  override protected val archive: IOutCreateArchiveGZip = archivePrototype.asInstanceOf[IOutCreateArchiveGZip]
}

final class ArchiveCreator7Z() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
} with AbstractArchiveCreator[ArchiveCreator7Z, CompressionEntry7Z]
  with SetEncryptHeader[ArchiveCreator7Z]
  with SetSolid[ArchiveCreator7Z]
  with SetLevel[ArchiveCreator7Z]
  with SetMultithreading[ArchiveCreator7Z] {
  override protected val archive: IOutCreateArchive7z = archivePrototype.asInstanceOf[IOutCreateArchive7z]
}

final class ArchiveCreatorBZip2() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.BZIP2
} with AbstractArchiveCreator[ArchiveCreatorBZip2, CompressionEntryBZip2]
  with SetLevel[ArchiveCreatorGZip] {
  override protected val archive: IOutCreateArchiveBZip2 = archivePrototype.asInstanceOf[IOutCreateArchiveBZip2]
}

final class ArchiveCreatorTar extends {
  override protected val format: ArchiveFormat = ArchiveFormat.TAR
} with AbstractArchiveCreator[ArchiveCreatorTar, CompressionEntryTar]
