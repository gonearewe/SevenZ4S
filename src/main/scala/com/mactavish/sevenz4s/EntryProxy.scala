package com.mactavish.sevenz4s

import net.sf.sevenzipjbinding.ISequentialInStream

/**
 * EntryProxy serves as an iterator of entries.
 *
 * @param producer entries to build from
 */
class EntryProxy[TEntry <: CompressionEntry](producer: Seq[TEntry]) {
  private var remains: Seq[TEntry] = producer

  def hasNext: Boolean = remains != Nil

  def next(): Option[TEntry] = {
    if (remains == Nil) None
    else {
      val a = remains.head
      remains = remains.tail
      Some(a)
    }
  }

  def nextSource(): Option[ISequentialInStream] = {
    next() match {
      case Some(entry) =>
        // directory doesn't contain source, skip
        if (entry.source == null) nextSource() else Some(entry.source)
      case None => None
    }
  }

  def reset(): Unit = {
    remains = producer
  }
}
