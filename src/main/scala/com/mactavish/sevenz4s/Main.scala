package com.mactavish.sevenz4s

import java.io.{File, RandomAccessFile}

import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream

object Main {
  def main(args: Array[String]): Unit = {
    val f = new File("E:\\backup.txt")
    new ArchiveCreator7Z().setHeaderEncryption(true).setPassword("123").setSolid(true).onEachEnd(
      ok=>println("one done")
    ).onProcess(
      (c,total)=>println(s"$c-$total")
    ).towards(new RandomAccessFile("test.7z","rw")).onTabulation(2){
      Seq(new CompressionEntry7Z(f.length(),new RandomAccessFileInStream(new RandomAccessFile(f,"rw")),"a.txt",false),
        new CompressionEntry7Z(f.length(),new RandomAccessFileInStream(new RandomAccessFile(f,"rw")),"b.txt",false))
    }.compress()

  }
}
