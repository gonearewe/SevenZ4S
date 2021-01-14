package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s.CompressionEntryBZip2
import com.mactavish.sevenz4s.adapter.AdapterBZip2
import com.mactavish.sevenz4s.creator.ArchiveCreatorFeature.SetLevel
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchiveBZip2, IOutItemBZip2}

final class ArchiveCreatorBZip2() extends {
  override protected val format: ArchiveFormat = ArchiveFormat.BZIP2
} with AbstractArchiveCreator[ArchiveCreatorBZip2]
  with AdapterBZip2
  with SetLevel[ArchiveCreatorBZip2] {
  override protected type TEntry = CompressionEntryBZip2
  override protected type TItem = IOutItemBZip2

  override protected val archive: IOutCreateArchiveBZip2 = archivePrototype.asInstanceOf[IOutCreateArchiveBZip2]
}
