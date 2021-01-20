package fun.mactavish.sevenz4s.adapter

import fun.mactavish.sevenz4s.CompressionEntryBZip2
import net.sf.sevenzipjbinding.IOutItemBZip2

trait AdapterBZip2 extends AbstractAdapter[CompressionEntryBZip2, IOutItemBZip2] {
  protected def adaptItemToEntry(item: IOutItemBZip2): CompressionEntryBZip2 = {
    CompressionEntryBZip2(
      source = null, // source is not required for library generated entries
      dataSize = item.getDataSize
    )
  }

  protected def adaptEntryToItem(entry: CompressionEntryBZip2, template: IOutItemBZip2): IOutItemBZip2 = {
    template.setDataSize(entry.dataSize)
    template
  }
}
