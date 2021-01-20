package fun.mactavish.sevenz4s.updater

import fun.mactavish.sevenz4s.CompressionEntry7Z
import fun.mactavish.sevenz4s.adapter.Adapter7Z
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItem7z}

/**
 * Concrete updater for 7Z archives.
 */
final class ArchiveUpdater7Z extends AbstractArchiveUpdater[ArchiveUpdater7Z]
  with Adapter7Z {
  override protected type TEntry = CompressionEntry7Z
  override protected type TItem = IOutItem7z

  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP

  override def ++=(entries: Seq[CompressionEntry7Z]): ArchiveUpdater7Z = super.++=(entries)

  override def append(entries: Seq[CompressionEntry7Z]): ArchiveUpdater7Z = super.append(entries)
}
