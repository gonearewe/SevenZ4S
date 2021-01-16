package com.mactavish.sevenz4s

import java.nio.file.{Files, Paths}

import scala.util.Random

object RandomString {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("usage: xxx  <length>  <file to write>")
      return
    }

    val len = args(0).toInt
    val file = Paths.get(args(1))

    val random = new Random()
    Files.writeString(file, random.nextString(len))
  }
}
