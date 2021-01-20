package fun.mactavish.sevenz4s

import java.io.File
import java.nio.file.{Files, Path}

import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{BeforeAll, TestInstance}

@TestInstance(Lifecycle.PER_CLASS)
trait AbstractTest {
  protected val path: Path = new File(getClass.getResource("/root").getFile).toPath

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
