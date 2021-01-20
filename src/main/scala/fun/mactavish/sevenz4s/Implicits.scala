package fun.mactavish.sevenz4s

import java.io.{InputStream, OutputStream}
import java.nio.file.Path

/**
 * A few implicit conversions that may come in handy.
 *
 * Note that we didn't use them in this library itself.
 */
object Implicits {
  implicit def pathSourceWrapper(a: Path): Left[Path, Nothing] = Left(a)

  implicit def inStreamSourceWrapper(a: InputStream): Right[Nothing, InputStream] = Right(a)

  implicit def outStreamSourceWrapper(a: OutputStream): Right[Nothing, OutputStream] = Right(a)
}
