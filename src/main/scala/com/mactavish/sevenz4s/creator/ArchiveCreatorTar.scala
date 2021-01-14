package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s.CompressionEntryTar
import com.mactavish.sevenz4s.adapter.AdapterTar
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemTar}

final class ArchiveCreatorTar extends {
  override protected val format: ArchiveFormat = ArchiveFormat.TAR
} with AbstractArchiveCreator[ArchiveCreatorTar]
  with AdapterTar {
  override protected type TEntry = CompressionEntryTar
  override protected type TItem = IOutItemTar
}
