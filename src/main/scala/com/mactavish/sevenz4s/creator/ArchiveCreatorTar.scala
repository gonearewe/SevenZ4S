package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s.CompressionEntryTar
import com.mactavish.sevenz4s.adapter.AdapterTar
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutItemTar}

/**
 * Concrete creator for Tar archives.
 */
final class ArchiveCreatorTar extends {
  /**
   * Use `early definition` syntax to make sure format get initialized before super trait.
   */
  override protected val format: ArchiveFormat = ArchiveFormat.TAR
} with AbstractArchiveCreator[ArchiveCreatorTar]
  with AdapterTar {
  override protected type TEntry = CompressionEntryTar
  override protected type TItem = IOutItemTar

  /**
   * Final stage of the archive creation, it will create an archive
   * with given entries. After this operation, this ArchiveCreator may
   * not be reused.
   *
   * @param entries entries in the expected archive to be created.
   */
  override def compress(entries: Seq[CompressionEntryTar]): Unit = super.compress(entries)
}
