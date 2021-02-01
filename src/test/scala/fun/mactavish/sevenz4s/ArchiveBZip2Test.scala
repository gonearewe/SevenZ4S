package fun.mactavish.sevenz4s

import java.nio.file.Files

import fun.mactavish.sevenz4s.Implicits._
import fun.mactavish.sevenz4s.creator.ArchiveCreatorBZip2
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
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
  @Order(2)
  def extractBZip2(): Unit = {
    new ArchiveExtractor()
      .from(path.resolveSibling("single.txt.bz2"))
      // `extractTo` takes output file `Path` as parameter
      .extractTo(path.resolveSibling("single extraction.txt"))
      .close() // ArchiveExtractor requires closing
  }

  @Test
  @Order(3)
  def checkResult(): Unit = {
    val expected = Files.readAllBytes(path.resolveSibling("single.txt"))
    val found = Files.readAllBytes(path.resolveSibling("single extraction.txt"))
    assertArrayEquals(expected, found)
  }
}
