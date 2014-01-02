package com.jluzon.billboards.objects

import com.jluzon.billboards.objects.Track

case class TrackChange(oldPosition: Int, newPosition: Int,track: Track) {
	//	def getOldPosition = oldPosition;
	//	def getNewPosition = newPosition;
	//	def getTrack = track;
	//	def getPositionChange = oldPosition - newPosition;
	override def toString() = 
			"Old: " + oldPosition + " New: " + newPosition + " - " + track;
}