package com.miraeclimate.climateconvergence.hycom

import java.io.{File, PrintWriter}
import java.net.URL

import com.miraeclimate.climateconvergence.sst.{SeaTrench, TrenchInfo}
import org.joda.time.DateTime

import scala.Some
import scala.collection.immutable.ListMap
import scala.io.Source

/**
 * Created by shawn on 15. 1. 9..
 */
object HycomByTrench {
  def main(args: Array[String]): Unit = {
    val (yAxis, xAxis) = loadXY
    "[11][22][33]".split(raw"[\[\]]").filter(_.trim.length > 0).foreach(println _)
    val trenches: List[SeaTrench] = TrenchInfo.build
    val trenchMap = buildTrenchMap(trenches, yAxis.toArray, xAxis.toArray)

//    (0 to 336).map { y =>
//      trenchMap.get((y,100))
//    }.flatten.foreach(println)

    val topDir: File = new File("/Users/shawn/temp/hycom/download/temp")
    processDir(topDir, trenchMap)
    val filepath = "/Users/shawn/temp/hycom/download/temp/2008/2008-204.txt"
    processFile(filepath, trenchMap)
  }

  def processDir(dir: File, trenchMap: Map[(Int,Int), Int]): Unit = {
    println("entering " + dir.getAbsolutePath)
    dir.listFiles().filter(_.isDirectory).foreach(processDir(_, trenchMap))
    dir.listFiles().filter(_.isFile).filter(_.getName.endsWith(".txt")).filterNot(_.isHidden).foreach(file => processFile(file.getAbsolutePath, trenchMap))
  }

  def processFile(filepath: String, trenchMap: Map[(Int,Int), Int]): Unit = {
    println("processing " + filepath)
    val date: DateTime = readDate(filepath)
    val valueList: List[CoordValue] = Source.fromFile(filepath).getLines().filter(_.startsWith("["))
      .map(line => processDataLine(date, trenchMap, line))
      .flatten
      .filter(cv => ((cv.st > 0) && (cv.value != FILLVALUE))).toList
    // valueList.foreach(println _)
    println("valueList length=" + valueList.length)

    //    val writer: PrintWriter = new PrintWriter(new File("/Users/shawn/temp/foo.csv"))
    //    valueList.foreach { cv =>
    //      val out = s"${cv.date},${cv.st},${cv.depth},${cv.value}"
    //      writer.println(out)
    //    }
    //    writer.close()

    val bySt: Map[(String, Int, Int), List[CoordValue]] = valueList.toList.groupBy(cv => (cv.date, cv.st, cv.depth))
    //    bySt.foreach(println)
    val avg = bySt.mapValues(cvList => cvList.map(v => v.value.toDouble).sum / cvList.length)
    //    println("bySt length=" + bySt.size)

    //    avg.keys.foreach(println)

    // avg.foreach(println)
    println(s"avg count for $date=" + avg.size)
    val svgWriter: PrintWriter = new PrintWriter(new File(filepath + ".avg.csv"))
    val sortedAvg = ListMap(avg.toSeq.sortBy(_._1):_*)

    sortedAvg.foreach { case(key,avg) =>
      val out = s"${key._1},${key._2},${key._3},${avg}"
      svgWriter.println(out)
    }
    svgWriter.close()
  }

  def readDate(filepath: String): DateTime = {
    val afterMTLine: Iterator[String] = Source.fromFile(filepath).getLines().dropWhile(!_.contains(".MT[1]"))
    val date: DateTime = new DateTime("1900-12-31").plusDays(afterMTLine.drop(1).toList.head.trim.toFloat.toInt)
    println("date: " + date)
    date
  }

  var lineCount = 0
  val FILLVALUE = "1.2676506E30"
  case class CoordValue(date: String, depth: Int, y: Int, x: Int, st: Int, value: String)
  def processDataLine(date: DateTime, trenchMap: Map[(Int,Int), Int], line: String): Array[CoordValue] = {
    val year: Int = date.getYear
    val month: Int = date.getMonthOfYear
    val day: Int = date.getDayOfMonth

    val values: Array[String] = line.split(",")
    val coord: Array[String] = values.head.split(raw"[\[\]]").filter(_.trim.length > 0)
    val (depth, y) = (coord(1),coord(2))
    val xValues = values.drop(1).map(_.trim)

    xValues.zipWithIndex.map(v => CoordValue(f"$year-$month%02d-$day%02d", depth.toInt, y.toInt, v._2, trenchMap.get((y.toInt,v._2)).getOrElse(-1), v._1))
//    println(lineCount + ": " + line)
//    lineCount += 1
  }

  def processVal(trenchMap: Map[(Int,Int), Int]) {
  }

  case class Grid(y: Int, x: Int, lat: BigDecimal, lng: BigDecimal, st: Option[SeaTrench])

  def buildTrenchMap(trenches: List[SeaTrench], yAxis: Array[(BigDecimal,Int)], xAxis: Array[(BigDecimal,Int)]): Map[(Int,Int), Int] = {
    val xySurface: Array[Grid] = for {
      y <- yAxis
      x <- xAxis
    } yield {
      val (lat, yIdx) = y
      val (lng, xIdx) = x
      val trench: Option[SeaTrench] = findTrenchFor(trenches, lat, lng)

      Grid(yIdx, xIdx, lat, lng, trench)
    }

    val xyTrenches = xySurface.filter(xy => hasTrench(xy.st))
    // xyTrenches.foreach(println)
    xyTrenches.map(xy => (xy.y, xy.x) -> xy.st.get.id).toMap
  }

  def hasTrench(st: Option[SeaTrench]): Boolean = st match {
    case Some(_) => true
    case _ => false
  }

  def findTrenchFor(trenches: List[SeaTrench], lat: BigDecimal, lng: BigDecimal): Option[SeaTrench] = {
    trenches.find(_.contains(lat.toFloat, lng.toFloat))
  }

  def loadXY = {
    val latFile: URL = getClass.getResource("/lat.csv")
    val lines: Iterator[String] = Source.fromURL(latFile).getLines()
    val yAxis = lines.map(BigDecimal(_))
    // yAxis.zipWithIndex.foreach(println _)

    val lngFile: URL = getClass.getResource("/lng.csv")
    val lngs: Array[String] = Source.fromURL(lngFile).getLines().mkString.split(',')
    val xAxis = lngs.filter(_.trim.length > 0).map(s => BigDecimal(s.trim))
    // xAxis.zipWithIndex.foreach(println _)

    (yAxis.zipWithIndex, xAxis.zipWithIndex)
  }

}
