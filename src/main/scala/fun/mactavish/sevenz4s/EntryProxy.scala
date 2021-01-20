package fun.mactavish.sevenz4s

import net.sf.sevenzipjbinding.ISequentialInStream

/**
 * EntryProxy serves as an iterator of entries.
 *
 * @param producer entries to build from
 */
private[sevenz4s] class EntryProxy[TEntry <: CompressionEntry](producer: Seq[TEntry]) {
  private var remains: Seq[TEntry] = producer

  def hasNext: Boolean = remains != Nil

  /**
   * Returns next entry and moves cursor one step forward.
   *
   * @return next entry
   */
  def next(): Option[TEntry] = {
    if (remains == Nil) None
    else {
      val a = remains.head
      remains = remains.tail
      Some(a)
    }
  }

  /**
   * Iterates next entry whose source is non-null, and returns that
   * source before move cursor one step forward.
   *
   * @return next non-null source
   */
  def nextSource(): Option[ISequentialInStream] = {
    next() match {
      case Some(entry) =>
        if (entry.source == null)
        // directory doesn't contain source, skip
          nextSource()
        else
          Some(SevenZ4S.open(entry.source))
      case None => None
    }
  }

  /**
   * Resets the cursor to the origin.
   */
  def reset(): Unit = {
    remains = producer
  }
}
