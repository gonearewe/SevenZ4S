package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s.CompressionEntry7Z
import com.mactavish.sevenz4s.adapter.Adapter7Z
import com.mactavish.sevenz4s.creator.ArchiveCreatorFeature.{SetEncryptHeader, SetLevel, SetMultithreading, SetSolid}
import net.sf.sevenzipjbinding.{ArchiveFormat, IOutCreateArchive7z, IOutItem7z}

final class ArchiveCreator7Z() extends {
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
}
