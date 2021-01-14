package com.mactavish.sevenz4s.updater

import com.mactavish.sevenz4s.CompressionEntry7Z
import com.mactavish.sevenz4s.adapter.Adapter7Z
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItem7z}

final class ArchiveUpdater7Z extends AbstractArchiveUpdater[ArchiveUpdater7Z]
  with Adapter7Z {
  override protected type TEntry = CompressionEntry7Z
  override protected type TItem = IOutItem7z

  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
}
