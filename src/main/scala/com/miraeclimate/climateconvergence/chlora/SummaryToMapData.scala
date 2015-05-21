package com.miraeclimate.climateconvergence.chlora

import java.io.FileWriter

import scala.io.Source

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
/**
 * Created by shawn on 15. 4. 2..
 */
object SummaryToMapData {
  def main(args: Array[String]) {

    case class Item(lat: Float, lng: Float, v: Float)
    implicit val formats = Serialization.formats(NoTypeHints)

    val jsonList = for {
      year <- 2013 to 2013;
      month <- 1 to 12
      path = f"/Users/shawn/temp/chlora/$year-${month}%02d.csv"
    } yield {
      val items = Source.fromFile(path).getLines().map { line =>
        val lineItems: Array[String] = line.split(",")
        val lat = lineItems(2)
        val lng = lineItems(3)
        val v = lineItems(5)
        Item(lat.toFloat, lng.toFloat, v.toFloat)
      }.toList
      val json = write(items) // emits json string
      val writer: FileWriter = new FileWriter(f"/Users/shawn/temp/chlora/mapdata-${year}-${month}%02d.json")
      writer.write(json)
      writer.close()
        json
    }
  }
}
