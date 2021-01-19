package com.mactavish.sevenz4s.updater

import com.mactavish.sevenz4s.CompressionEntryZip
import com.mactavish.sevenz4s.adapter.AdapterZip
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemZip}

/**
 * Concrete updater for Zip archives.
 */
final class ArchiveUpdaterZip extends AbstractArchiveUpdater[ArchiveUpdaterZip]
  with AdapterZip {
  override protected type TEntry = CompressionEntryZip
  override protected type TItem = IOutItemZip

  override protected val format: ArchiveFormat = ArchiveFormat.ZIP

  override def ++=(entries: Seq[CompressionEntryZip]): ArchiveUpdaterZip = super.++=(entries)

  override def append(entries: Seq[CompressionEntryZip]): ArchiveUpdaterZip = super.append(entries)
}
