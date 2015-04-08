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

    val items = Source.fromFile("/Users/shawn/temp/chlora/summary-2013-08.csv").getLines().map { line =>
      val lineItems: Array[String] = line.split(",")
      val lat = lineItems(2)
      val lng = lineItems(3)
      val v = lineItems(5)
      Item(lat.toFloat, lng.toFloat, v.toFloat)
    }.toList

    implicit val formats = Serialization.formats(NoTypeHints)
    val json: String = write(items)
    println(json)

    val writer: FileWriter = new FileWriter("/Users/shawn/temp/chlora/mapdata-2013-08.json")
    items.foreach { item =>

    }
    writer.close()
  }
}
