package fun.mactavish.sevenz4s

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import fun.mactavish.sevenz4s.Implicits._
import fun.mactavish.sevenz4s.creator.ArchiveCreatorGZip
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

import scala.util.Random

object ArchiveByteArrayTest {
  @Test
  def arrayStreamTest(): Unit = {
    val random = new Random()
    val src = random.nextBytes(1 << 25)
    val compression = new ByteArrayOutputStream()
    val dst = new ByteArrayOutputStream()

    new ArchiveCreatorGZip()
      .towards(compression)
      .onProcess((completed, total) => println(s"$completed of $total"))
      .onEachEnd(println(_))
      .compress(CompressionEntryGZip(
        dataSize = src.length,
        source = new ByteArrayInputStream(src),
        path = "bytes"
      ))

    new ArchiveExtractor()
      .from(new ByteArrayInputStream(compression.toByteArray))
      .extractTo(dst)

    assertArrayEquals(dst.toByteArray, src)
  }
}
