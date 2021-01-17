package com.mactavish.sevenz4s

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

    //val f = new File("test.7z")
    //new ArchiveUpdater7Z().from(f)
    //  .towards(f)
    //  .removeWhere {
    //    entry =>
    //      entry.path == "a.txt"
    //  }.removeWhere {
    //  entry =>
    //    entry.path == "b.txt"
    //}.append(Seq(CompressionEntry7Z(
    //  "afgvcj".getBytes.length,
    //  new ByteArrayStream("afgvcj".getBytes, false),
    //  "append.txt", isDir = false)))
    //  .update {
    //    x =>
    //      println(x)
    //      x
    //  }
  }
}
