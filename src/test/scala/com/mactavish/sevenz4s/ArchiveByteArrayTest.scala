package com.mactavish.sevenz4s

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream}

import com.mactavish.sevenz4s.Implicits._
import com.mactavish.sevenz4s.creator.ArchiveCreatorBZip2
import com.mactavish.sevenz4s.extractor.ArchiveExtractor
import com.mactavish.sevenz4s.updater.ArchiveUpdaterBZip2
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

    new ArchiveCreatorBZip2()
      .towards(compression)
      .compress(CompressionEntryBZip2(
        dataSize = src.length,
        source = new ByteArrayInputStream(src)
      ))

    // pure pressure test for Updater
    new ArchiveUpdaterBZip2()
      .from(new ByteArrayInputStream(compression.toByteArray))
      .towards(OutputStream.nullOutputStream())
      .update(identity) // update nothing

    new ArchiveExtractor()
      .from(new ByteArrayInputStream(compression.toByteArray))
      .extractTo(dst)

    assertArrayEquals(dst.toByteArray, src)
  }
}
