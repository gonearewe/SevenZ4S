package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s.CompressionEntryGZip
import com.mactavish.sevenz4s.adapter.AdapterGZip
import com.mactavish.sevenz4s.creator.ArchiveCreatorFeature.SetLevel
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchiveGZip, IOutItemGZip}

/**
 * Concrete creator for GZip archives.
 */
final class ArchiveCreatorGZip() extends {
  /**
   * Use `early definition` syntax to make sure format get initialized before super trait.
   */
  override protected val format: ArchiveFormat = ArchiveFormat.GZIP
} with AbstractArchiveCreator[ArchiveCreatorGZip]
  with AdapterGZip
  with SetLevel[ArchiveCreatorGZip] {
  override protected type TEntry = CompressionEntryGZip
  override protected type TItem = IOutItemGZip

  override protected val archive: IOutCreateArchiveGZip = archivePrototype.asInstanceOf[IOutCreateArchiveGZip]
}
