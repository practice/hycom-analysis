package com.miraeclimate.climateconvergence.hycom

import java.io.File

import scala.sys.process.Process

/**
 * Created by shawn on 15. 1. 12..
 */
object CatCsv {
  val baseDir = new File("/Users/shawn/temp/hycom")

  def main(args: Array[String]): Unit = {
    new File("/Users/shawn/temp/hycom/download").listFiles().toList.filter(_.isDirectory).foreach(processKindDirs(_))
    // renameFiles(new File("/Users/shawn/temp/hycom/download"))
  }

  def renameFiles(dir: File): Unit = {
    dir.listFiles().filter(_.isDirectory).foreach(renameFiles(_))
    dir.listFiles().filter(_.isFile).filter(file => file.getName.contains(".txt")).foreach { file =>
      val dir = file.getParentFile
      val extpos = file.getName.indexOf(".txt")
      val name: String = file.getName.substring(0, extpos)
      val fileNo: Int = name.substring(5).toInt
      val newFile = new File(dir, f"${dir.getName}-${fileNo}%03d${file.getName.substring(extpos)}")
      println(s"renaming ${file.getAbsolutePath} to ${newFile.getAbsolutePath}")
      if (newFile.getName != file.getName)
        file.renameTo(newFile)
    }
  }

  def processKindDirs(dir: File): Unit = {
    dir.listFiles().toList.filter(_.isDirectory).foreach(processYearDir(dir, _))
  }

  def processYearDir(kindDir: File, yearDir: File): Unit = {
    (Process(Seq("/bin/bash", "-c", "cat *.csv"), yearDir)  #> new File(baseDir, kindDir.getName + "-" + yearDir.getName + ".csv") ).!
  }
}
