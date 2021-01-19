package com.mactavish.sevenz4s.updater

import com.mactavish.sevenz4s.CompressionEntryBZip2
import com.mactavish.sevenz4s.adapter.AdapterBZip2
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemBZip2}

/**
 * Concrete updater for BZip2 archives.
 */
final class ArchiveUpdaterBZip2 extends AbstractArchiveUpdater[ArchiveUpdaterBZip2]
  with AdapterBZip2 {

  override protected type TEntry = CompressionEntryBZip2
  override protected type TItem = IOutItemBZip2

  override protected val format: ArchiveFormat = ArchiveFormat.BZIP2
}
