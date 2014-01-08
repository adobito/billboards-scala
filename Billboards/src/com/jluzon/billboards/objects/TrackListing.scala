package com.jluzon.billboards.objects

import java.sql.Date

case class TrackListing(position: Int,track: Track, date: Date) {
	//	def getOldPosition = oldPosition;
	//	def getNewPosition = newPosition;
	//	def getTrack = track;
	//	def getPositionChange = oldPosition - newPosition;
//	override def toString() = 
//			"Old: " + oldPosition + " New: " + newPosition + " - " + track;
}