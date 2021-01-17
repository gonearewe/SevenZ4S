package com.mactavish.sevenz4s.creator

import java.io.RandomAccessFile
import java.nio.file.Path

import com.mactavish.sevenz4s.{CompressionEntry, EntryProxy, ProgressTracker, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.{OutItemFactory, RandomAccessFileOutStream}
import net.sf.sevenzipjbinding.util.ByteArrayStream

trait AbstractArchiveCreator[E <: AbstractArchiveCreator[E]] {
  this: E =>

  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  private var dst: ISequentialOutStream = _
  /**
   * `archivePrototype` will be casted to specific type in subclasses
   * so that their features can be enabled.
   */
  protected val archivePrototype: IOutCreateArchive[TItem] =
    SevenZip.openOutArchive(format: ArchiveFormat).asInstanceOf[IOutCreateArchive[TItem]]
  /**
   * If creator writes archive stream into file, `file` stores the handle
   * so that it can be closed afterwards. If not, `file` remains null.
   */
  private var file: RandomAccessFile = _
  /**
   * If `password` is set, then the archive will be encrypted
   * with given password.
   */
  private var password: String = _
  private var onProcess: ProgressTracker = (_, _) => {}
  private var onEachEnd: Boolean => Unit = _ => {}

  /**
   * Subclass override `format` to specify which format of archive it
   * intends to create.
   *
   * Subclass must make sure format get initialized before this trait,
   * (by early definition, maybe) as archivePrototype will use it during
   * trait initialization.
   */
  protected val format: ArchiveFormat
  /**
   * `usedOnce` marks whether `compress` method has been called,
   * if true, then this creator can't be reused.
   */
  private var usedOnce: Boolean = false

  /**
   * Final stage of the archive creation, it will create an archive
   * with given entry. After this operation, this ArchiveCreator may
   * not be reused.
   *
   * @param entry entry in the expected archive to be created.
   */
  def compress(entry: TEntry): Unit = compress(Seq(entry))

  /**
   * Final stage of the archive creation, it will create an archive
   * with given entries. After this operation, this ArchiveCreator may
   * not be reused.
   *
   * Note that some archive formats (`bzip2`, `gzip`) only supports compression
   * of single archive entry (thus they're usually used along with `tar`).
   * So `compress` with `Seq[TEntry]` is made `protected`, only those supporting
   * multi-archive override it to `public`.
   *
   * @param entries entries in the expected archive to be created.
   */
  protected def compress(entries: Seq[TEntry]): Unit = {
    if (dst == null) throw SevenZ4SException("archive output not set, did you call `towards`?")

    val numEntry = entries.size
    val entryProxy = new EntryProxy(entries)

    // print trace for debugging
    //archivePrototype.setTrace(true)

    archivePrototype.createArchive(dst, numEntry, new IOutCreateCallback[TItem] with ICryptoGetTextPassword {
      private var total: Long = -1

      override def setTotal(l: Long): Unit = this.total = l

      override def setCompleted(l: Long): Unit = if (this.total != -1) onProcess(l, this.total)

      override def setOperationResult(b: Boolean): Unit = onEachEnd(b)

      override def getItemInformation(
                                       i: Int,
                                       outItemFactory: OutItemFactory[TItem]
                                     ): TItem = {
        val templateItem = outItemFactory.createOutItem()
        entryProxy.next() match {
          case Some(entry) => adaptEntryToItem(entry, templateItem)
          case None =>
            throw SevenZ4SException(s"only $i entries provided, $numEntry expected")
        }
      }

      /**
       * `getStream` is called after all(`numEntry` times) `getItemInformation` calls,
       * but note that, it could be called for times fewer than `numEntry`,
       * since directory entry (whose `isDir` == true) will be skipped, meaning
       * `i` can be discontinuous. And that's why we use `entryProxy.nextSource()`
       * instead of `entryProxy.next()`.
       *
       * @param i index of entry
       * @return where 7Z engine gets data stream
       */
      override def getStream(i: Int): ISequentialInStream = {
        if (!entryProxy.hasNext) entryProxy.reset()
        entryProxy.nextSource() match {
          case Some(src) => src
          case None =>
            throw SevenZ4SException("not enough entries containing source are provided")
        }
      }

      /**
       * If null is passed, simply means no password, and it won't crash at runtime.
       */
      override def cryptoGetTextPassword(): String = password
    })

    entries.foreach(e => e.close())
    archivePrototype.close()
    if (this.file != null)
      this.file.close()
  }

  def towards(dst: Array[Byte]): E = {
    if (dst == null) throw SevenZ4SException("dst has already been set")

    this.dst = new ByteArrayStream(dst, false, Int.MaxValue)
    // down-cast to actual ArchiveCreator in order to
    // enable chain methods calling on concrete ArchiveCreator.
    this
  }

  def towards(dst: Path): E = {
    if (dst == null) throw SevenZ4SException("dst has already been set")

    // must open in "rw" mode
    this.file = new RandomAccessFile(dst.toFile, "rw")
    this.dst = new RandomAccessFileOutStream(this.file)
    this
  }

  def setPassword(passwd: String): E = {
    this.password = passwd
    this
  }

  def onProcess(f: ProgressTracker): E = {
    //if(onProcess==null) throw SevenZ4SException("onProcess callback function has already been set")
    this.onProcess = f
    this
  }

  def onEachEnd(f: Boolean => Unit): E = {
    //if(onEachEnd==null) throw SevenZ4SException("onEachEnd callback function has already been set")
    this.onEachEnd = f
    this
  }

  protected def adaptItemToEntry(item: TItem): TEntry

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem
}
