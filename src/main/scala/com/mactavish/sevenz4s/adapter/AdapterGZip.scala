package com.mactavish.sevenz4s.adapter

import com.mactavish.sevenz4s.CompressionEntryGZip
import net.sf.sevenzipjbinding.IOutItemGZip

trait AdapterGZip {
  protected def adaptItemToEntry(item: IOutItemGZip): CompressionEntryGZip = {
    CompressionEntryGZip(
      source = null,
      dataSize = item.getDataSize,
      path = item.getPropertyPath,
      lastModificationTime = item.getPropertyLastModificationTime
    )
  }

  protected def adaptEntryToItem(entry: CompressionEntryGZip, template: IOutItemGZip): IOutItemGZip = {
    template.setDataSize(entry.dataSize)
    template.setPropertyPath(entry.path)
    template.setPropertyLastModificationTime(entry.lastModificationTime)
    template
  }
}
