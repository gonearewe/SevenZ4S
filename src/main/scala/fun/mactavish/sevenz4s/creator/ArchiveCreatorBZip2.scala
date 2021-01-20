package fun.mactavish.sevenz4s.creator

import fun.mactavish.sevenz4s.CompressionEntryBZip2
import fun.mactavish.sevenz4s.adapter.AdapterBZip2
import fun.mactavish.sevenz4s.creator.ArchiveCreatorFeature.SetLevel
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchiveBZip2, IOutItemBZip2}

/**
 * Concrete creator for BZip2 archives.
 */
final class ArchiveCreatorBZip2() extends {
  // early definition must be used to ensure `AbstractArchiveCreator`
  // knows exactly format to initialize `archivePrototype`
  override protected val format: ArchiveFormat = ArchiveFormat.BZIP2
} with AbstractArchiveCreator[ArchiveCreatorBZip2]
  with AdapterBZip2
  with SetLevel[ArchiveCreatorBZip2] {
  override protected type TEntry = CompressionEntryBZip2
  override protected type TItem = IOutItemBZip2

  override protected val archive: IOutCreateArchiveBZip2 = archivePrototype.asInstanceOf[IOutCreateArchiveBZip2]
}
