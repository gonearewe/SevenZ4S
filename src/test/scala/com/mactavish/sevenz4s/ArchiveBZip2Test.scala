package com.mactavish.sevenz4s

import java.nio.file.Files

import com.mactavish.sevenz4s.Implicits._
import com.mactavish.sevenz4s.creator.ArchiveCreatorBZip2
import com.mactavish.sevenz4s.extractor.ArchiveExtractor
import com.mactavish.sevenz4s.updater.ArchiveUpdaterBZip2
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.{Order, Test, TestMethodOrder}

@TestMethodOrder(classOf[OrderAnnotation])
object ArchiveBZip2Test extends AbstractTest {
  @Test
  @Order(1)
  def createBZip2(): Unit = {
    val entry = SevenZ4S.getBZip2EntryFrom(path.resolveSibling("single.txt"))
    new ArchiveCreatorBZip2()
      // bzip2 decides the content's file name on archive's file name
      .towards(path.resolveSibling("single.txt.bz2"))
      .compress(entry)
  }

  @Test
  //@Disabled
  @Order(2)
  def updateBZip2(): Unit = {
    // this is actually copy
    new ArchiveUpdaterBZip2()
      .from(path.resolveSibling("single.txt.bz2"))
      .towards(path.resolveSibling("new.txt.bz2"))
      .update(identity) // update nothing
  }

  @Test
  @Order(3)
  def extractBZip2(): Unit = {
    new ArchiveExtractor()
      .from(path.resolveSibling("single.txt.bz2"))
      // `extractTo` takes output file `Path` as parameter
      .extractTo(path.resolveSibling("single extraction.txt"))
      .close() // ArchiveExtractor requires closing
  }

  @Test
  @Order(4)
  def checkResult(): Unit = {
    val expected = Files.readAllBytes(path.resolveSibling("single.txt"))
    val found = Files.readAllBytes(path.resolveSibling("single extraction.txt"))
    assertArrayEquals(expected, found)
  }
}
