package fun.mactavish.sevenz4s.adapter

import fun.mactavish.sevenz4s.CompressionEntryTar
import net.sf.sevenzipjbinding.IOutItemTar

private[sevenz4s] trait AdapterTar extends AbstractAdapter[CompressionEntryTar, IOutItemTar] {
  protected def adaptItemToEntry(item: IOutItemTar): CompressionEntryTar = {
    CompressionEntryTar(
      source = null, // source is not required for library generated entries
      dataSize = item.getDataSize,
      path = item.getPropertyPath,
      isDir = item.getPropertyIsDir,
      lastModificationTime = item.getPropertyLastModificationTime,
      user = item.getPropertyUser,
      group = item.getPropertyGroup,
      symLink = item.getPropertySymLink,
      hardLink = item.getPropertyHardLink
    )
  }

  protected def adaptEntryToItem(entry: CompressionEntryTar, template: IOutItemTar): IOutItemTar = {
    template.setDataSize(entry.dataSize)
    template.setPropertyPath(entry.path)
    template.setPropertyIsDir(entry.isDir)
    template.setPropertyLastModificationTime(entry.lastModificationTime)
    template.setPropertyUser(entry.user)
    template.setPropertyGroup(entry.group)
    template.setPropertySymLink(entry.symLink)
    template.setPropertyHardLink(entry.hardLink)
    template
  }
}
