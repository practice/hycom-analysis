package com.miraeclimate.climateconvergence.hycom

import java.io.{File, PrintWriter, StringWriter}

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import org.joda.time.DateTime

/**
 * Created by shawn on 15. 1. 5..
 */

case object WhatIsNextDate
case class NextDate(exptName: String, year: Int, mt: Int, date: DateTime)
case object StartMsg

class DateProducer(startingDate: String) extends Actor {
  val startDate = new DateTime(startingDate)
  /*
expt_90.6
  2008: 0..104
  2009: 0..125
expt_90.8
  2009: 0..238
  2010: 0..364
  2011: 0..1
expt_90.9
  2011: 0..362
  2012: 0..365
  2013: 0..231
expt_91.0
  2013: 0..132
  2014: 0..93
expt_91.1
  2014: 0..270
 */

  val exports = Array(("expt_90.6", 2008, 104), ("expt_90.6", 2009, 125),
    ("expt_90.8", 2009, 238), ("expt_90.8", 2010, 364), ("expt_90.8", 2011, 1),
    ("expt_90.9", 2011, 362), ("expt_90.9", 2012, 365), ("expt_90.9", 2013, 231),
    ("expt_91.0", 2013, 132), ("expt_91.0", 2014, 93), ("expt_91.1", 2014, 270) )

  val days = exports.map(expt => (0 to expt._3).map((expt._1, expt._2, _))).flatten
  val exptStart = new DateTime("2008-09-18")
  var expt = days.zipWithIndex.map(day => NextDate(day._1._1, day._1._2, day._1._3, exptStart.plusDays(day._2)))

  // expt.foreach(println _)
  expt = expt.filterNot(nextDate => nextDate.date.isBefore(startDate))
  println(s"expt count = ${expt.length}")

  override def receive: Receive = {
    case WhatIsNextDate =>
      val head = expt.head
      expt = expt.tail
      sender ! head

  }
}

class Downloader(dateProducer: ActorRef) extends Actor {
  def downloadForDate(exptName: String, year: Int, mt: Int, date: DateTime, tryCount: Int = 5) {
    if (date.getYear == 2015) {
      println("done.")
      context.stop(self)
      return
    }
    Thread.sleep(200)
    val dateSeq = date.getDayOfYear

    println(f"downloading $year.${date.getMonthOfYear}.${date.getDayOfMonth}-${dateSeq}%03d from ${exptName},mt=${mt}")
//                http://tds.hycom.org/thredds/dodsC/GLBa0.08/expt_90.6/2008/temp.ascii?temperature[0:1:0][0:1:0][0:1:0][0:1:0]
    val url = f"http://tds.hycom.org/thredds/dodsC/GLBa0.08/${exptName}/${year}/temp.ascii?temperature[${mt}:1:${mt}][0:1:6][1819:1:2155][559:1:824]"
    // http://tds.hycom.org/thredds/dodsC/GLBa0.08/expt_91.1/2014/salt.ascii?salinity[0:1:0][0:1:6][1819:1:2155][559:1:824]
    // http://tds.hycom.org/thredds/dodsC/GLBa0.08/expt_91.1/2014/uvel.ascii?u[0:1:0][0:1:0][0:1:0][0:1:0]
    // http://tds.hycom.org/thredds/dodsC/GLBa0.08/expt_91.1/2014/vvel.ascii?v[0:1:0][0:1:0][0:1:0][0:1:0]
    val outputPath = f"/Users/shawn/temp/hycom/download/temp/${year}/${year}-$dateSeq.txt"
    try {
      val content = getUrl(url)
      writeFile(outputPath, content)
    } catch {
      case ioe: java.io.IOException =>
        val content: StringWriter = new StringWriter()
        val w: PrintWriter = new PrintWriter(content)
        ioe.printStackTrace(w)
        w.flush()
        writeFile(outputPath + "-error-", date.toString + "\n" + content.toString)
        downloadForDate(exptName, year, mt, date)
      case ste: java.net.SocketTimeoutException =>
        val content: StringWriter = new StringWriter()
        val w: PrintWriter = new PrintWriter(content)
        ste.printStackTrace(w)
        w.flush()
        writeFile(outputPath + "-error-", date.toString + "\n" + content.toString)
        downloadForDate(exptName, year, mt, date)
    }
  }

  def writeFile(path: String, content: String) {
    val outFile = new File(path)
    val dir = outFile.getParentFile
    dir.mkdirs()
    val writer: PrintWriter = new PrintWriter(path)
    writer.print(content)
    writer.close()
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def getUrl(url: String, connectTimeout:Int =30000, readTimeout:Int =30000, requestMethod: String = "GET"): String = {
    import java.net.{HttpURLConnection, URL}
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

  override def receive: Receive = {
    case StartMsg =>
//      println("starting")
      dateProducer ! WhatIsNextDate
    case NextDate(exptName: String, year: Int, mt: Int, date: DateTime) =>
      downloadForDate(exptName, year, mt, date)
      dateProducer ! WhatIsNextDate
  }
}

object DownloadHycom {
  def main(args: Array[String]) {
    // an actor needs an ActorSystem
    val system = ActorSystem("DownloadHycomSystem")
    // create and start the actor
//    val dateProducer = system.actorOf(Props(new DateProducer("2008-09-18")), name = "DateProducer")
    val dateProducer = system.actorOf(Props(new DateProducer("2012-07-21")), name = "DateProducer")
    println("building downloaders")
    val downloaders = for (i <- 1 to 10) yield system.actorOf(Props(new Downloader(dateProducer)), name=s"dl-$i")
    downloaders.foreach(_ ! StartMsg)
//    var a = Array(1,2,3,4)
//    a = a.filter(_ > 2)
//    a.foreach(println _)

    // 2008.9.18 ~
//    val exprDir = List("expt_90.3", "expt_90.6", "expt_90.8", "expt_90.9", "expt_91.0", "expt_91.1")

    // url = "http://tds.hycom.org/thredds/dodsC/GLBa0.08/expt_91.1/2014/temp/archv.2014_095_00_3zt.nc.ascii?temperature[0:1:0][0:1:6][1819:1:2155][559:1:824]"
//    val date = new DateTime("2008-09-18") // this is start date.
    val date = new DateTime("2009-06-09") // this is start date.
//    val date = new DateTime("2013-08-21") // this is start date.
    println(date.getYear) // 2008
    println(date.getMonthOfYear) // 9
    println(date.getDayOfMonth) // 18
    println(date.getDayOfYear) // 262

    system.awaitTermination()

    system.shutdown()
  }
  

}
