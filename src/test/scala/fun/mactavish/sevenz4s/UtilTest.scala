package fun.mactavish.sevenz4s

import java.nio.file.Files

import fun.mactavish.sevenz4s.Implicits._
import org.junit.jupiter.api.Assertions.{assertArrayEquals, assertEquals}
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

object UtilTest extends AbstractTest {
  @Test
  def test7Z(): Unit = {
    val output = path.resolveSibling("util test/7z")

    SevenZ4S.compress(ArchiveFormat.SEVEN_Z, path, output)
    val f = output.resolve("root.7z")

    assumeTrue(Files.exists(f))
    assertEquals(SevenZ4S.formatOf(f), ArchiveFormat.SEVEN_Z)

    SevenZ4S.extract(f, output)
    AbstractTest.verifyDirsAreEqual(path, output.resolve("root"))
  }

  @Test
  def testZip(): Unit = {
    val output = path.resolveSibling("util test/zip")

    SevenZ4S.compress(ArchiveFormat.ZIP, path, output)
    val f = output.resolve("root.zip")

    assumeTrue(Files.exists(f))
    assertEquals(SevenZ4S.formatOf(f), ArchiveFormat.ZIP)

    SevenZ4S.extract(f, output)
    AbstractTest.verifyDirsAreEqual(path, output.resolve("root"))
  }

  @Test
  def testTar(): Unit = {
    val output = path.resolveSibling("util test/tar")

    SevenZ4S.compress(ArchiveFormat.TAR, path, output)
    val f = output.resolve("root.tar")

    assumeTrue(Files.exists(f))
    assertEquals(SevenZ4S.formatOf(f), ArchiveFormat.TAR)

    SevenZ4S.extract(f, output)
    AbstractTest.verifyDirsAreEqual(path, output.resolve("root"))
  }

  @Test
  def testGZip(): Unit = {
    val output = path.resolveSibling("util test/gzip")

    val singleFile = path.resolveSibling("single.txt")
    SevenZ4S.compress(ArchiveFormat.GZIP, singleFile, output)
    val f = output.resolve("single.txt.gz")

    assumeTrue(Files.exists(f))
    assertEquals(SevenZ4S.formatOf(f), ArchiveFormat.GZIP)

    SevenZ4S.extract(f, output)
    assertArrayEquals(Files.readAllBytes(singleFile), Files.readAllBytes(output.resolve("single.txt")))
  }

  @Test
  def testBZip2(): Unit = {
    val output = path.resolveSibling("util test/bzip2")

    val singleFile = path.resolveSibling("single.txt")
    SevenZ4S.compress(ArchiveFormat.BZIP2, singleFile, output)
    val f = output.resolve("single.txt.bz2")

    assumeTrue(Files.exists(f))
    assertEquals(SevenZ4S.formatOf(f), ArchiveFormat.BZIP2)

    SevenZ4S.extract(f, output)
    assertArrayEquals(Files.readAllBytes(singleFile), Files.readAllBytes(output.resolve("single.txt")))
  }
}
