package fun.mactavish.sevenz4s.updater

import fun.mactavish.sevenz4s.CompressionEntryZip
import fun.mactavish.sevenz4s.adapter.AdapterZip
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemZip}

/**
 * Concrete updater for Zip archives.
 *
 * TODO: There're some critical problems with `ArchiveUpdaterZip`.
 */
private[sevenz4s] final class ArchiveUpdaterZip extends AbstractArchiveUpdater[ArchiveUpdaterZip]
  with AdapterZip {
  override protected type TEntry = CompressionEntryZip
  override protected type TItem = IOutItemZip

  override protected val format: ArchiveFormat = ArchiveFormat.ZIP

  override def ++=(entries: Seq[CompressionEntryZip]): ArchiveUpdaterZip = super.++=(entries)

  override def append(entries: Seq[CompressionEntryZip]): ArchiveUpdaterZip = super.append(entries)
}
