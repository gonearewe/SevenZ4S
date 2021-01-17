package com.mactavish.sevenz4s

import java.io.File
import java.nio.file.Files

import com.mactavish.sevenz4s.Implicits._
import com.mactavish.sevenz4s.updater.{ArchiveUpdater7Z, ArchiveUpdaterBZip2}
import org.junit.jupiter.api.Test

object ArchiveUpdaterTest {
  private val path = new File(getClass.getResource("/root.7z").getFile).toPath

  @Test
  def update7Z(): Unit = {
    val replacement = path.resolveSibling("replace.txt")

    val updater = new ArchiveUpdater7Z()
      .from(path) // update itself
      .withPassword("12345")
      .update {
        entry =>
          if (entry.path == "root\\a.txt") { // path separator is '\'
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
    // notice that file with the same name is allowed
    updater += SevenZ4S.get7ZEntriesFrom(replacement).head
  }

  @Test
  def updateBZip2(): Unit = {
    new ArchiveUpdaterBZip2()
      .from(path.resolveSibling("single.txt.bz2"))
      .towards(path.resolveSibling("new.txt.bz2"))
      .update(identity) // update nothing
  }
}
