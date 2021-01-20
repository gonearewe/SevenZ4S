package fun.mactavish.sevenz4s.updater

import fun.mactavish.sevenz4s.CompressionEntryGZip
import fun.mactavish.sevenz4s.adapter.AdapterGZip
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemGZip}

/**
 * Concrete updater for GZip archives.
 */
final class ArchiveUpdaterGZip extends AbstractArchiveUpdater[ArchiveUpdaterGZip]
  with AdapterGZip {
  override protected type TEntry = CompressionEntryGZip
  override protected type TItem = IOutItemGZip

  override protected val format: ArchiveFormat = ArchiveFormat.GZIP
}
