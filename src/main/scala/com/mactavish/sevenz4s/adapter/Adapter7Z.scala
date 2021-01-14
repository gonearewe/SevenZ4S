package com.mactavish.sevenz4s.adapter

import com.mactavish.sevenz4s.CompressionEntry7Z
import net.sf.sevenzipjbinding.IOutItem7z

trait Adapter7Z {
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
      source = null,
      dataSize = item.getDataSize,
      path = item.getPropertyPath,
      isDir = item.getPropertyIsDir,
      lastModificationTime = item.getPropertyLastModificationTime,
      isAnti = item.getPropertyIsAnti
    )
  }
}
