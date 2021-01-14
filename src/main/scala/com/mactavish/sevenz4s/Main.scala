package com.mactavish.sevenz4s

import java.io.{File, RandomAccessFile}

import com.mactavish.sevenz4s.updater.ArchiveUpdater7Z
import net.sf.sevenzipjbinding.impl.{RandomAccessFileInStream, RandomAccessFileOutStream}

object Main {
  def main(args: Array[String]): Unit = {
    //val f = new File("E:\\backup.txt")
    //new ArchiveCreator7Z().setHeaderEncryption(true).setSolid(true).onEachEnd(
    //  ok=>println("one done")
    //).onProcess(
    //  (c,total)=>println(s"$c-$total")
    //).towards(new RandomAccessFile("test.7z","rw")).onTabulation(2){
    //  Seq(new CompressionEntry7Z(f.length(),new RandomAccessFileInStream(new RandomAccessFile(f,"rw")),"a.txt",false),
    //    new CompressionEntry7Z(f.length(),new RandomAccessFileInStream(new RandomAccessFile(f,"rw")),"b.txt",false))
    //}.compress()
    val f = new RandomAccessFile(new File("test.7z"), "r")
    val to = new RandomAccessFile(new File("test.7z"), "rw")
    new ArchiveUpdater7Z().from(new RandomAccessFileInStream(f))
      .towards(new RandomAccessFileOutStream(to))
      .removeWhere {
        entry =>
          entry.path == "a.txt"
      }
  }
}
