package fun.mactavish.sevenz4s.updater

import fun.mactavish.sevenz4s.CompressionEntryTar
import fun.mactavish.sevenz4s.adapter.AdapterTar
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemTar}

/**
 * Concrete updater for Tar archives.
 */
final class ArchiveUpdaterTar extends AbstractArchiveUpdater[ArchiveUpdaterTar]
  with AdapterTar {
  override protected type TEntry = CompressionEntryTar
  override protected type TItem = IOutItemTar

  override protected val format: ArchiveFormat = ArchiveFormat.TAR

  override def ++=(entries: Seq[CompressionEntryTar]): ArchiveUpdaterTar = super.++=(entries)

  override def append(entries: Seq[CompressionEntryTar]): ArchiveUpdaterTar = super.append(entries)
}
