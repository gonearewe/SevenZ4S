package com.mactavish.sevenz4s

import java.io.File

import com.mactavish.sevenz4s.extractor.ArchiveExtractor
import org.junit.jupiter.api.Test

object ArchiveExtractorTest {
  private val path = new File(getClass.getResource("/root.7z").getFile).toPath

  @Test
  def extract7Z(): Unit = {
    new ArchiveExtractor()
      .from(path)
      .withPassword("12345")
      .onEachEnd(println(_))
      .foreach { entry =>
        println(entry.path)
        if (entry.path == "root\\b.txt")
          entry.extractTo(path.resolveSibling("b extraction.txt"))
      }.extractTo(path.resolveSibling("extraction")) // `onEachEnd` callback only triggers on `extractTo`
  }

  @Test
  def extractBZip2(): Unit = {
    new ArchiveExtractor()
      .from(path.resolveSibling("single.txt.bz2"))
      .extractTo(path.resolveSibling("single extraction.txt"))
  }
}
