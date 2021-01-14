package com.mactavish.sevenz4s.adapter

import com.mactavish.sevenz4s.CompressionEntryTar
import net.sf.sevenzipjbinding.IOutItemTar

trait AdapterTar {
  protected def adaptItemToEntry(item: IOutItemTar): CompressionEntryTar = ???

  protected def adaptEntryToItem(entry: CompressionEntryTar, template: IOutItemTar): IOutItemTar = ???
}
