package com.miraeclimate.climateconvergence.chlora

import java.io.{PrintWriter, File}

import com.miraeclimate.climateconvergence.sst.{TrenchInfo, SeaTrench}

import scala.io.Source

/**
 * Created by shawn on 15. 4. 1..
 */
case class Chlora(year: String, month: String, lat: String, lng: String, trenchId: Int, v: String)
case class ChFile(year: String, month: String, path: String)

object ChloraByTrench {
  def main(args: Array[String]): Unit = {
    val trenches: List[SeaTrench] = TrenchInfo.build

    def findTrenchFor(trenches: List[SeaTrench], lat: BigDecimal, lng: BigDecimal): Option[SeaTrench] = {
      trenches.find(_.contains(lat.toFloat, lng.toFloat))
    }

    def processFile(file: ChFile): List[Chlora] = {
      println(s"processing $file")
      val lines: Iterator[String] = Source.fromFile(new File(file.path)).getLines()
      val header: Array[String] = lines.next().split(",")

      //    println(header)
      def processLine(line: String): List[Chlora] = {
        val vals: Array[String] = line.split(",")
        val lat: String = vals.head
        val items: Array[Chlora] = for {
          v <- vals.drop(1).zipWithIndex
          lng = header((v._2.toInt + 1))
          trench: Option[SeaTrench] = findTrenchFor(trenches, BigDecimal(lat), BigDecimal(lng))
          if trench.isDefined
        } yield Chlora(file.year, file.month, lat, lng, trench.get.id, v._1)
        items.filter(_.v != "99999.0").toList.filter(isKoreanArea)
      }
      lines.drop(1).map(processLine).flatten.toList
    }

    // val chloras = processFile("/Users/shawn/Downloads/RenderData.csv");
    // println(chloras)

    def listFiles: List[ChFile] = {
      val filesCandidates =
        for {
          year <- 2008 to 2014;
          month <- 1 to 12
          path = f"/Users/shawn/temp/chlora/MY1DMM_CHLORA_$year-${month}%02d-01_rgb_720x360.SS.CSV"
          if new File(path).exists()
        } yield ChFile("" + year, f"$month%02d", path)
      filesCandidates.toList
    }

    val chloras: List[Chlora] = listFiles.flatMap(processFile)

    val writer: PrintWriter = new PrintWriter(new File("/Users/shawn/temp/chlora/summary.csv"))
    chloras.sortWith { case (lh, rh) =>
      lh.year <= rh.year && lh.month <= rh.month && lh.trenchId <= rh.trenchId
    }.foreach { ch =>
      writer.println(s"${ch.year},${ch.month},${ch.lat},${ch.lng},${ch.trenchId},${ch.v}")
    }
    writer.close()
  }
  
  def isKoreaLon(lon: String): Boolean = {
    lon.toFloat > 119.0 && lon.toFloat < 139.0
  }
  def isKoreaLat(lat: String): Boolean = {
    lat.toFloat > 25.0 && lat.toFloat < 46.0
  }

  def isKoreanArea(c: Chlora): Boolean = {
    isKoreaLat(c.lat) && isKoreaLon(c.lng)
  }
}
