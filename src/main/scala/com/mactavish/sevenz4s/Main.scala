package com.mactavish.compress_sharp.lib

import java.io.{File, RandomAccessFile}

import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.util.ByteArrayStream

object Main {
  def main(args: Array[String]): Unit = {
    val f = new File("E:\\backup.txt")
    new ArchiveCreator7Z().setLevel(5).onEachEnd(
      ok=>println("one done")
    ).onProcess(
      (c,total)=>println(s"$c-$total")
    ).towards(new RandomAccessFile("test.7z","rw")).onTabulation(2){
      Seq(new CompressionEntry7Z(f.length(),new RandomAccessFileInStream(new RandomAccessFile(f,"rw")),"a.txt",false),
        new CompressionEntry7Z(f.length(),new RandomAccessFileInStream(new RandomAccessFile(f,"rw")),"b.txt",false))
    }.compress()

  }
}
