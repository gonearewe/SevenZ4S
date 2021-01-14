package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s.CompressionEntryGZip
import com.mactavish.sevenz4s.adapter.AdapterGZip
import com.mactavish.sevenz4s.creator.ArchiveCreatorFeature.SetLevel
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchiveGZip, IOutItemGZip}


final class ArchiveCreatorGZip() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.GZIP
} with AbstractArchiveCreator[ArchiveCreatorGZip]
  with AdapterGZip
  with SetLevel[ArchiveCreatorGZip] {
  override protected type TEntry = CompressionEntryGZip
  override protected type TItem = IOutItemGZip

  override protected val archive: IOutCreateArchiveGZip = archivePrototype.asInstanceOf[IOutCreateArchiveGZip]
}
