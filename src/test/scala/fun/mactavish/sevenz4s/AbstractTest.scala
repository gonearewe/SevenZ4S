package fun.mactavish.sevenz4s

import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

import org.junit.jupiter.api.Assertions.{assertArrayEquals, assertEquals}
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{BeforeAll, TestInstance}

@TestInstance(Lifecycle.PER_CLASS)
trait AbstractTest {
  protected val path: Path = new File(getClass.getResource("/root").getFile).toPath

  /**
   * Clear test directory (build\resources\test) at the start of each test class.
   *
   * `Junit` will run multiple tests in different processes at one time, so if one
   * test fails, it may prevent other processes from clearing directory (file lock).
   */
  @BeforeAll
  def clearFolder(): Unit = {
    def delete(path: File): Unit = {
      if (path.isDirectory)
        path.listFiles() foreach delete
      Files.delete(path.toPath)
    }

    path.getParent.toFile.listFiles().filterNot { f =>
      Set("root", "single.txt", "replace.txt", "expected") contains f.getName
    }.foreach(f => delete(f))
  }
}

object AbstractTest {
  /**
   * Verify two directories are equal, which means they share the same file
   * structure and all files in these two directories have the same content.
   *
   * @param expected  path of one directory
   * @param generated path of the other directory
   */
  def verifyDirsAreEqual(expected: Path, generated: Path): Unit = {
    Files.walkFileTree(expected, new SimpleFileVisitor[Path]() {

      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val result = super.preVisitDirectory(dir, attrs)
        // get the relative file name from path "expected"
        val relativize = expected.relativize(dir)
        // construct the path for the counterpart file in "generated"
        val otherDir = generated.resolve(relativize).toFile
        assertEquals(dir.toFile.list.toList, otherDir.list.toList)
        result
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val result = super.visitFile(file, attrs)
        val relativize = expected.relativize(file)
        val fileInOther = generated.resolve(relativize).toFile
        val expectedContents = Files.readAllBytes(file)
        val generatedContents = Files.readAllBytes(fileInOther.toPath)
        assertArrayEquals(expectedContents, generatedContents)
        result
      }
    })
  }
}
