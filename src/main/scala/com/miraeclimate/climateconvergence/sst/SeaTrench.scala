package com.miraeclimate.climateconvergence.sst

/**
 * Created by shawn on 14. 11. 18..
 */
case class SeaTrench(val id: Int, val latMin: Float, val lngMin: Float) {
  def contains(lat: Float, lng: Float): Boolean = {
    (lat > latMin && lat <= (latMin + 0.5f)) && (lng > lngMin && lng <= (lngMin + 0.5f))
  }
}
