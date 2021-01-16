package com.mactavish.sevenz4s

import java.io.File

import com.mactavish.sevenz4s.creator.{ArchiveCreator7Z, ArchiveCreatorBZip2}
import org.junit.jupiter.api.Test

object ArchiveCreatorTest {
  private val path = new File(getClass.getResource("/root").getFile).toPath

  @Test
  def create7Z(): Unit = {
    val entries = SevenZ4S.get7ZEntriesFrom(path)
    new ArchiveCreator7Z()
      .towards(path.resolveSibling("root.7z").toFile)
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
        println(s"$completed-$total")
    }.compress(entries)
  }

  //def createZip():Unit = {
  //  new ArchiveCreatorZip()
  //    .towards(path.resolveSibling("root.zip").toFile)
  //    .setLevel(2)
  //    .compress(entries)
  //}

  @Test
  def createBZip2(): Unit = {
    val entry = SevenZ4S.getBZip2EntryFrom(path.resolveSibling("single.txt"))
    new ArchiveCreatorBZip2()
      // bzip2 decides the content
      .towards(path.resolveSibling("single.txt.bz2").toFile)
      .compress(entry)
  }
}
