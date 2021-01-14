package com.mactavish.sevenz4s.creator

import com.mactavish.sevenz4s._
import com.mactavish.sevenz4s.adapter.AdapterZip
import com.mactavish.sevenz4s.creator.ArchiveCreatorFeature._
import net.sf.sevenzipjbinding._

final class ArchiveCreatorZip extends {
  /**
   * Use `early definition` syntax to make sure format get initialized before super trait.
   */
  override protected val format: ArchiveFormat = ArchiveFormat.ZIP
} with AbstractArchiveCreator[ArchiveCreatorZip]
  with AdapterZip
  with SetLevel[ArchiveCreatorZip] {
  override protected type TEntry = CompressionEntryZip
  override protected type TItem = IOutItemZip
  /**
   * Cast and expose `archive` in order to set possible features.
   */
  override protected val archive: IOutCreateArchiveZip = archivePrototype.asInstanceOf[IOutCreateArchiveZip]
}







