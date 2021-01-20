package fun.mactavish.sevenz4s.adapter

import fun.mactavish.sevenz4s.CompressionEntry
import net.sf.sevenzipjbinding.IOutItemBase

/**
 * `Adapter` serves as a bridge between `sevenzipjbinding` package's
 * item representations and this library's item representations (named as `entry`).
 *
 * `AbstractAdapter` is a generic trait used to restrict adapters on type level.
 * You may find duplicated code among its subclasses, for some archive formats
 * share similar properties, but they're too vulnerable to draw abstraction from.
 * So stay with the duplication.
 *
 * @tparam T subclass of `CompressionEntry`
 * @tparam R subclass of `IOutItemBase`
 */
trait AbstractAdapter[T <: CompressionEntry, R <: IOutItemBase] {
  /**
   * `adaptEntryToItem` is an adapter from `CompressionEntry` to `IOutItem`,
   * it requires an default instance of `IOutItem` as a template so that
   * it can work without the knowledge of the construction of it.
   *
   * @param entry    input `CompressionEntry` for adapting
   * @param template an default instance of `IOutItem`
   * @return `IOutItem` whose properties are already set according to `entry`
   */
  protected def adaptEntryToItem(entry: T, template: R): R

  /**
   * `adaptItemToEntry` is an adapter from `IOutItem` to `CompressionEntry`,
   * and unlike `adaptEntryToItem`, it knows how to construct `CompressionEntry`,
   * so template isn't required.
   *
   * @param item input `IOutItem` for adapting
   * @return `CompressionEntry` whose properties are already set according to `item`
   */
  protected def adaptItemToEntry(item: R): T
}
