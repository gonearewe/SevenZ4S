package fun.mactavish.sevenz4s.adapter

import fun.mactavish.sevenz4s.CompressionEntryZip
import net.sf.sevenzipjbinding.IOutItemZip

private[sevenz4s] trait AdapterZip extends AbstractAdapter[CompressionEntryZip, IOutItemZip] {
  protected def adaptItemToEntry(item: IOutItemZip): CompressionEntryZip = {
    CompressionEntryZip(
      source = null, // source is not required for library generated entries
      dataSize = item.getDataSize,
      path = item.getPropertyPath,
      lastModificationTime = item.getPropertyLastModificationTime,
      isDir = item.getPropertyIsDir,
      lastAccessTime = item.getPropertyLastAccessTime,
      creationTime = item.getPropertyCreationTime
    )
  }

  protected def adaptEntryToItem(entry: CompressionEntryZip, template: IOutItemZip): IOutItemZip = {
    template.setDataSize(entry.dataSize)
    template.setPropertyPath(entry.path)
    template.setPropertyIsDir(entry.isDir)
    template.setPropertyLastModificationTime(entry.lastModificationTime)
    template.setPropertyLastAccessTime(entry.lastAccessTime)
    template.setPropertyCreationTime(entry.creationTime)
    template
  }
}
