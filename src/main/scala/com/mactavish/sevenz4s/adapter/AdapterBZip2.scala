package com.mactavish.sevenz4s.adapter

import com.mactavish.sevenz4s.CompressionEntryBZip2
import net.sf.sevenzipjbinding.IOutItemBZip2

trait AdapterBZip2 {
  protected def adaptItemToEntry(item: IOutItemBZip2): CompressionEntryBZip2 = {
    CompressionEntryBZip2(
      source = null,
      dataSize = item.getDataSize
    )
  }

  protected def adaptEntryToItem(entry: CompressionEntryBZip2, template: IOutItemBZip2): IOutItemBZip2 = {
    template.setDataSize(entry.dataSize)
    template
  }
}
