package com.mactavish.sevenz4s

import java.io.File

import com.mactavish.sevenz4s.Implicits._
import com.mactavish.sevenz4s.creator.ArchiveCreatorBZip2
import com.mactavish.sevenz4s.extractor.ArchiveExtractor
import com.mactavish.sevenz4s.updater.ArchiveUpdaterBZip2
import org.junit.jupiter.api.Test

object ArchiveBZip2Test {
  private val path = new File(getClass.getResource("/root").getFile).toPath

  @Test
  def createBZip2(): Unit = {
    val entry = SevenZ4S.getBZip2EntryFrom(path.resolveSibling("single.txt"))
    new ArchiveCreatorBZip2()
      // bzip2 decides the content's file name on archive's file name
      .towards(path.resolveSibling("single.txt.bz2"))
      .compress(entry)
  }

  @Test
  def updateBZip2(): Unit = {
    // this is actually copy
    new ArchiveUpdaterBZip2()
      .from(path.resolveSibling("single.txt.bz2"))
      .towards(path.resolveSibling("new.txt.bz2"))
      .update(identity) // update nothing
  }

  @Test
  def extractBZip2(): Unit = {
    new ArchiveExtractor()
      .from(path.resolveSibling("single.txt.bz2"))
      // `extractTo` takes output file `Path` as parameter
      .extractTo(path.resolveSibling("single extraction.txt"))
  }
}
