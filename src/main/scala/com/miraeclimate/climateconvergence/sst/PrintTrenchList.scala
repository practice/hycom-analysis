package com.miraeclimate.climateconvergence.sst

/**
 * Created by shawn on 15. 3. 2..
 */
object PrintTrenchList {
  def main (args: Array[String]) {
    val trenches = TrenchInfo.build

//    Util.printAll(trenches)
    val sortedList: List[SeaTrench] = trenches.sortWith((a, b) => a.id < b.id)
    // Util.printAll(sortedList)
    sortedList.foreach { it =>
      println(s"${it.id},${it.latMin},${it.lngMin}")
    }
  }

}
