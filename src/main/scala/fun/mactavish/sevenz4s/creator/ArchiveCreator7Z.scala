package fun.mactavish.sevenz4s.creator

import fun.mactavish.sevenz4s.CompressionEntry7Z
import fun.mactavish.sevenz4s.adapter.Adapter7Z
import fun.mactavish.sevenz4s.creator.ArchiveCreatorFeature.{SetEncryptHeader, SetLevel, SetMultithreading, SetSolid}
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchive7z, IOutItem7z}

/**
 * Concrete creator for 7Z archives.
 */
final class ArchiveCreator7Z() extends {
  // early definition must be used to ensure `AbstractArchiveCreator`
  // knows exactly format to initialize `archivePrototype`
  override protected val format: ArchiveFormat = ArchiveFormat.SEVEN_ZIP
} with AbstractArchiveCreator[ArchiveCreator7Z]
  with Adapter7Z
  with SetEncryptHeader[ArchiveCreator7Z]
  with SetSolid[ArchiveCreator7Z]
  with SetLevel[ArchiveCreator7Z]
  with SetMultithreading[ArchiveCreator7Z] {
  override protected type TEntry = CompressionEntry7Z
  override protected type TItem = IOutItem7z

  override protected val archive: IOutCreateArchive7z = archivePrototype.asInstanceOf[IOutCreateArchive7z]

  /**
   * Final stage of the archive creation, it will create an archive
   * with given entries. After this operation, this ArchiveCreator may
   * not be reused.
   *
   * @param entries entries in the expected archive to be created.
   */
  override def compress(entries: Seq[CompressionEntry7Z]): Unit = super.compress(entries)
}
