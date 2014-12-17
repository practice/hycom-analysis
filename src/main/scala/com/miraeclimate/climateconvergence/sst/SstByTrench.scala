package com.miraeclimate.climateconvergence.sst

import java.io.{File, PrintWriter}

import scala.io.Source

/**
 * Created by shawn on 14. 11. 18..
 */
object SstByTrench {

  def main (args: Array[String]) {
    val trenches = TrenchInfo.build

    // Util.printAll(trenches)

    for (year <- 2004 until 2014)
      extractSst(trenches, "/Users/shawn/temp/nc.kr", s"sst.day.mean.${year}.v2.nc.kr")
  }

  def extractSst(trenches: List[SeaTrench], baseDir: String, filename: String): Unit = {
    val source = Source.fromFile(new File(new File(baseDir), filename))
    // println("doing filename: " + filename)
    val outFile: File = new File(new File(baseDir), filename + ".csv")
    val out: PrintWriter = new PrintWriter(outFile)

    for (line <- source.getLines()) {
      if (line.contains("time") && line.contains("lat") && line.contains("lon") && line.contains("sst")) {
        val items: Array[String] = line.split("""\s+""")

        var dateIndex: Int = 0
        var lat: Float = 0f
        var lng: Float = 0f
        var temperature: String = ""
        for (item <- items) {
          val splittedItem: Array[String] = item.split("=", -1)
          if (item.startsWith("time")) {
            dateIndex = splittedItem(1).toInt
          } else if (item.startsWith("lat")) {
            lat = splittedItem(1).toFloat
          } else if (item.startsWith("lon")) {
            lng = splittedItem(1).toFloat
          } else if (item.startsWith("sst")) {
            temperature = splittedItem(1)
          }
        }
        val sst: SeaSurfaceTemperature = new SeaSurfaceTemperature(dateIndex, lat, lng, temperature)

        val trench: Option[SeaTrench] = findSeaTrench(trenches, sst)
        if (!trench.isEmpty && sst.temperature != "-9.96921e+36") {
          val outLine: String = f"${sst.date}%s,${trench.get.id}%d,${sst.lat}%.3f,${sst.lng}%.3f,${sst.temperature}%s"
          // val outLine: String = String.format(, sst.date, trench.get.id, sst.lat, sst.lng, sst.temperature)
          out.println(outLine)
          println(outLine)
        }
      }
    }
    out.close()
    source.close()
  }

  def findSeaTrench(trenches: List[SeaTrench], sst: SeaSurfaceTemperature): Option[SeaTrench] = {
    trenches.find(_.contains(sst.lat, sst.lng))
  }

}
