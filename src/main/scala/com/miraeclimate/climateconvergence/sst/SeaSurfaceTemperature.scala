package com.miraeclimate.climateconvergence.sst

import java.time.{LocalDate, Month}

/**
 * Created by shawn on 14. 11. 18..
 */
case class SeaSurfaceTemperature(dateIndex: Int, lat: Float, lng: Float, temperature: String) {
  def date: String = {
    val date1800: LocalDate = LocalDate.of(1800, Month.JANUARY, 1)
    date1800.plusDays(dateIndex).toString
  }
}
