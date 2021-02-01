package fun.mactavish.sevenz4s.creator

import java.io.{Closeable, OutputStream}
import java.nio.file.Path

import fun.mactavish.sevenz4s.{CompressionEntry, SevenZ4S, SevenZ4SException}
import net.sf.sevenzipjbinding._
import net.sf.sevenzipjbinding.impl.OutItemFactory

import scala.collection.mutable

/**
 * A skeleton for `ArchiveCreator`, most methods and fields are implemented.
 * `ArchiveCreator` must set up the destination for output archive stream,
 * and some configurations are optional. Call `compress` with entries will
 * trigger the actual compression procedure, where some hooks are provided.
 *
 * @tparam E subclass should pass it's own type here to enable
 *           chained method calls
 */
private[sevenz4s] trait AbstractArchiveCreator[E <: AbstractArchiveCreator[E]] {
  this: E =>

  protected type TEntry <: CompressionEntry
  protected type TItem <: IOutItemBase

  private var dst: Either[Path, OutputStream] = _
  /**
   * `archivePrototype` will be casted to specific type in subclasses
   * so that their features can be enabled.
   */
  protected val archivePrototype: IOutCreateArchive[TItem] =
    SevenZip.openOutArchive(format: ArchiveFormat).asInstanceOf[IOutCreateArchive[TItem]]
  /**
   * If `password` is set, then the archive will be encrypted
   * with given password.
   */
  private var password: String = _
  private var onProcess: (Long, Long) => Unit = (_, _) => {}
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
    if (this.usedOnce)
      throw SevenZ4SException("you've called `compress`, `ArchiveCreator` can't be reused")
    if (this.dst == null)
      throw SevenZ4SException("archive output not set, try to call `towards` before `compress`")

    val output: IOutStream with Closeable = SevenZ4S.open(this.dst)
    val entries_ = entries.toIndexedSeq
    val entryStreams = mutable.HashSet[Closeable]()

    // print trace for debugging
    // archivePrototype.setTrace(true)

    archivePrototype.createArchive(output, entries_.size, new IOutCreateCallback[TItem] with ICryptoGetTextPassword {
      private var total: Long = -1

      override def setTotal(l: Long): Unit = this.total = l

      override def setCompleted(l: Long): Unit = if (this.total != -1) onProcess(l, this.total)

      /**
       * Gets called after each call of `getStream`.
       *
       * @param b operation result
       */
      override def setOperationResult(b: Boolean): Unit = onEachEnd(b)

      override def getItemInformation(
                                       i: Int,
                                       outItemFactory: OutItemFactory[TItem]
                                     ): TItem = {
        val templateItem = outItemFactory.createOutItem()
        adaptEntryToItem(entries_(i), templateItem)
      }

      /**
       * For `Tar` format, the order of `getStream` and `getItemInformation`
       * method calls is complex.
       *
       * For other formats, `getStream` is called after all(`numEntry` times) `getItemInformation` calls,
       * but note that, it could be called for times fewer than `numEntry`,
       * since directory entry (whose `isDir` == true) will be skipped, meaning
       * `i` can be discontinuous. And that's why we use `entryProxy.nextSource()`
       * instead of `entryProxy.next()`.
       *
       * @param i index of entry
       * @return where 7Z engine gets data stream
       */
      override def getStream(i: Int): ISequentialInStream = {
        if (entries_(i).source == null) {
          // for `Tar` format, no index will be skipped, provide null explicitly
          null
        } else {
          val src = SevenZ4S.open(entries_(i).source)
          entryStreams.add(src)
          src
        }
      }

      /**
       * If null is passed, simply means no password, and it won't crash at runtime.
       */
      override def cryptoGetTextPassword(): String = password
    }

    )

    entryStreams.foreach(c => c.close())
    this.usedOnce = true
    // compress can only be called once, safe to close the archive
    this.archivePrototype.close()
    output.close()
  }

  /**
   * Set a destination for output archive stream, and you shouldn't reset it afterwards.
   * Passing a `Path` results in creation of a new archive file,
   * and passing an `OutputStream` will see the flush of stream, in which case
   * we won't close it, you have to close the `OutputStream` on your own.
   *
   * @param dst the destination for output archive stream
   * @return creator itself so that method calls can be chained
   */
  def towards(dst: Either[Path, OutputStream]): E = {
    if (dst == null) throw SevenZ4SException("`towards` doesn't accept null parameter")
    if (this.dst != null)
      throw SevenZ4SException("`dst` has already been set, `towards` isn't allowed to be called for multiple times")

    this.dst = dst
    this
  }

  /**
   * Sets up password protection for this archive.
   *
   * @param passwd non-nullable password to set up
   * @return creator itself so that method calls can be chained
   */
  def setPassword(passwd: String): E = {
    if (passwd == null) throw SevenZ4SException("null passwd is meaningless")
    this.password = passwd
    this
  }

  /**
   * Provides callback triggered during compression operation.
   *
   * First parameter of the callback is the number of bytes completed compression,
   * the second one is the total number of bytes to compress. Together,
   * they can describe the progress of compression.
   *
   * Note that the time and frequency of this callback's trigger is unknown.
   * And it's only relevant with the bytes to compress, nothing to do with
   * the entries.
   *
   * @param progressTracker (completed, total) => `Unit`
   * @return creator itself so that method calls can be chained
   */
  def onProcess(progressTracker: (Long, Long) => Unit): E = {
    if (progressTracker == null) throw SevenZ4SException("`onProcess` doesn't accept null callback")
    this.onProcess = progressTracker
    this
  }

  /**
   * Provides callback triggered after each solid operation.
   *
   * Usually, the operation of compression of an nonempty entry is
   * solid, after which this callback gets triggered and supplies
   * a boolean parameter indicating whether this operation is successful.
   *
   * Note that directory entry and file entry whose size is zero normally
   * doesn't trigger this callback.
   *
   * @param f isSuccessful => `Unit`
   * @return creator itself so that method calls can be chained
   */
  def onEachEnd(f: Boolean => Unit): E = {
    if (f == null) throw SevenZ4SException("`onEachEnd` doesn't accept null callback")
    this.onEachEnd = f
    this
  }

  protected def adaptItemToEntry(item: TItem): TEntry

  protected def adaptEntryToItem(entry: TEntry, template: TItem): TItem
}
