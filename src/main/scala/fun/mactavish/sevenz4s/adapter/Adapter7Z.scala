package fun.mactavish.sevenz4s.adapter

import fun.mactavish.sevenz4s.CompressionEntry7Z
import net.sf.sevenzipjbinding.IOutItem7z

private[sevenz4s] trait Adapter7Z extends AbstractAdapter[CompressionEntry7Z, IOutItem7z] {
  protected def adaptEntryToItem(entry: CompressionEntry7Z, template: IOutItem7z): IOutItem7z = {
    template.setDataSize(entry.dataSize)
    template.setPropertyPath(entry.path)
    template.setPropertyIsDir(entry.isDir)
    template.setPropertyLastModificationTime(entry.lastModificationTime)
    template.setPropertyIsAnti(entry.isAnti)
    template
  }

  protected def adaptItemToEntry(item: IOutItem7z): CompressionEntry7Z = {
    CompressionEntry7Z(
      source = null, // source is not required for library generated entries
      dataSize = item.getDataSize,
      path = item.getPropertyPath,
      isDir = item.getPropertyIsDir,
      lastModificationTime = item.getPropertyLastModificationTime,
      isAnti = item.getPropertyIsAnti
    )
  }
}
