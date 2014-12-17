package com.miraeclimate.climateconvergence.hycom

import java.io.{File, PrintWriter}

import scala.io.BufferedSource

/**
 * Created by shawn on 14. 12. 16..
 */
object RoughPrinter {
  private final val baseDir: String = "/Users/shawn/temp/hycom"
  private final val inputFile: String = "2013_001"
  private final val outputFile: String = "2013_001.rough"

  def hasDifferentFields(lastTuples: Array[(String, String, String)], tuples: Array[(String, String, String)]): Boolean = {
    !(lastTuples.map(_._1).toSet == tuples.map(_._1).toSet)
  }

  def under360(s: String): String = {
    val f: Float = s.toFloat
    if (f > 360) under360((f-360).toString)
    else f.toString
  }

  def isLatLngAndInKorea(tuples: Array[(String, String, String)]): Boolean = {
    val set: Set[String] = tuples.map(_._1).toSet
    val fieldsOk = set == Set("X", "Y", "Latitude") || set == Set("X","Y","Longitude")
    if (fieldsOk) {
      val latOption: Option[(String, String, String)] = tuples.filter(_._1 == "Latitude").headOption
      val lat = latOption match {
        case Some(("Latitude",index,value)) => value.toFloat
        case None => 0f
        case _ => 0f
      }
      if (lat > 24.375 && lat < 46.125) return true

      val lngOption: Option[(String, String, String)] = tuples.filter(_._1 == "Longitude").headOption
      val lng = lngOption match {
        case Some(("Longitude",index,value)) => under360(value).toFloat
        case None => 0f
        case _ => 0f
      }
      if (lng > 118.875 && lng < 140.125) return true
      return false
    } else {
      false
    }
  }

  def main(args: Array[String]) {
    val writer: PrintWriter = new PrintWriter(new File(new File(baseDir), outputFile))
    val file: BufferedSource = io.Source.fromFile(new File(new File(baseDir), inputFile))

    val fieldToTuple: (String) => (String, String, String) = (rawField) => {
//      println(rawField)
      val left: Int = rawField.indexOf('[')
//      println(left)
      val right: Int = rawField.indexOf(']')
//      println(right)
      (rawField.substring(0, left), rawField.substring(left+1,right), rawField.substring(right+2))
    }

    var lastTuples = new Array[(String, String, String)](0)
    var lastLine = ""
    def printTuples(tuples: Array[(String, String, String)]): String = {
      tuples.map(_.toString).mkString(" ")
    }
    for (line <- file.getLines()) {
      val tuples: Array[(String, String, String)] = line.split(" ", -1).map(_.trim).filter(_.length > 0).map(fieldToTuple)
      if (hasDifferentFields(lastTuples, tuples)) {
        lastLine = printTuples(lastTuples)
        val currentLine: String = printTuples(tuples)
        writer.println(lastLine)
        writer.println(currentLine)
        println(lastLine)
        println(currentLine)
      }
      if (isLatLngAndInKorea(tuples)) {
        writer.println(printTuples(tuples))
        println(printTuples(tuples))
      }
      lastTuples = tuples
    }
    file.close()
  }
}
