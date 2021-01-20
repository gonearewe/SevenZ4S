package fun.mactavish.sevenz4s

import java.nio.file.Files

import fun.mactavish.sevenz4s.Implicits._
import fun.mactavish.sevenz4s.creator.ArchiveCreator7Z
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
import fun.mactavish.sevenz4s.updater.ArchiveUpdater7Z
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.{Order, Test, TestMethodOrder}


@TestMethodOrder(classOf[OrderAnnotation])
object Archive7ZTest extends AbstractTest {
  @Test
  @Order(1)
  def create7Z(): Unit = {
    val entries = SevenZ4S.get7ZEntriesFrom(path)
    new ArchiveCreator7Z()
      .towards(path.resolveSibling("root.7z"))
      // archive-relevant features
      .setLevel(5)
      .setSolid(true)
      .setHeaderEncryption(true)
      .setPassword("12345")
      .setThreadCount(3)
      .onEachEnd {
        ok =>
          if (ok)
            println("one success")
          else
            println("one failure")
      }.onProcess {
      (completed, total) =>
        println(s"$completed of $total")
    }.compress(entries)
  }

  @Test
  @Order(2)
  def update7Z(): Unit = {
    val replacement = path.resolveSibling("replace.txt")

    val updater = new ArchiveUpdater7Z()
      .from(path.resolveSibling("root.7z")) // update itself
      .withPassword("12345")
      .update {
        entry =>
          if (entry.path == "root\\a.txt") { // path separator is OS-relevant
            entry.copy(
              dataSize = Files.size(replacement), // remember to update size
              source = replacement, // implicit conversion happens here
              path = "root\\a replaced.txt" // file name contains space
            )
          } else {
            entry
          }
      }

    updater.removeWhere(entry => entry.path == "root\\sub\\deeper\\c.txt")
    // directory can not be deleted until its contents have all been deleted,
    // otherwise, it ignores the request silently rather than raise an exception.
    updater.removeWhere(entry => entry.path == "root\\sub\\deeper")
    updater += SevenZ4S.get7ZEntriesFrom(replacement).head
    // notice that file with the same name is allowed,
    // but may be overwritten by OS's file system during extraction
    updater += SevenZ4S.get7ZEntriesFrom(replacement).head
  }

  @Test
  @Order(3)
  def extract7Z(): Unit = {
    new ArchiveExtractor()
      .from(path.resolveSibling("root.7z"))
      .withPassword("12345")
      .onEachEnd(println(_))
      .foreach { entry =>
        println(entry.path)
        if (entry.path == "root\\b.txt") {
          // extract independently
          entry.extractTo(path.resolveSibling("b extraction.txt"))
        }
      }
      // `extractTo` takes output folder `Path` as parameter
      // `onEachEnd` callback only triggers on `extractTo`
      .extractTo(path.resolveSibling("extraction"))
      .close() // ArchiveExtractor requires closing
  }

  @Test
  @Order(4)
  def checkResults(): Unit = {
    // check single extraction result
    val bExpected = Files.readAllBytes(path.resolve("b.txt"))
    val bFound = Files.readAllBytes(path.resolveSibling("b extraction.txt"))
    assertArrayEquals(bExpected, bFound)

    // check extraction structure
    val structureExpected = Array(
      "extraction", "extraction/replace.txt", "extraction/root",
      "extraction/root/sub", "extraction/root/a replaced.txt",
      "extraction/root/b.txt", "extraction/root/sub/empty.txt"
    )
    structureExpected foreach { p =>
      assumeTrue(Files.exists(path.resolveSibling(p)), s"$p not found")
    }

    // check extraction contents
    structureExpected filter { p => p.endsWith(".txt") } foreach { p =>
      val expected = Files.readAllBytes(path.resolveSibling(p.replace("extraction", "expected")))
      val found = Files.readAllBytes(path.resolveSibling(p))
      assertArrayEquals(expected, found, s"unexpected content in $p")
    }
  }
}
