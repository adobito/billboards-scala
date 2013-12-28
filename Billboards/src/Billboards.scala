import java.net.URL

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML

import com.github.sendgrid.SendGrid

object Main {
    final val toEmail = "jesus.e.luzon@gmail.com"
	def main(args: Array[String]) {
		val billboardsURLString : String = "http://www1.billboard.com/rss/charts/hot-100" 
				val billboardsURL = new URL(billboardsURLString)
	val xmlString = Source.fromInputStream(billboardsURL.openStream()).getLines().mkString("\n")
	val xml = XML.load(billboardsURL.openStream())
	val items: NodeSeq = (((xml \ "channel") \ "item"))

	val trackList = new Array[Track](101)
	items.foreach(x => (makeTrack(x,trackList)))

	def equalsNotNull(first: Track, second: Track):Boolean = {
		val opt = Option(first)
				val opt2 = Option(second)
				return opt.isDefined && opt2.isDefined && first.equals(second) 
	}
	val shuff = Random.shuffle(trackList.toList);
	val changeList = compareArrays(trackList, shuff.toArray)
	val stringBuilder = new StringBuilder();
	changeList.foreach(x => stringBuilder ++= x.track.toString + "\n");
	val sendGrid = new SendGrid(SendGridCredentials.username,SendGridCredentials.password);
	sendGrid.addTo(toEmail);
	sendGrid.setSubject("Billboard Hot 100 changes");
	sendGrid.setFrom("admin@jluzon.com");
	sendGrid.setFromName("Jesus Luzon");
	sendGrid.setText(stringBuilder.toString)
	sendGrid.send();
	
	}

	def compareArrays(oldTrackList: Array[Track], newTrackList: Array[Track]): List[TrackChange] = {
		val list: ListBuffer[TrackChange] = new ListBuffer[TrackChange]();
		for(i <- 1 to 100) {
			val oldTrackIndex = oldTrackList.indexOf(newTrackList(i)) 
					if( oldTrackIndex != i) {
						list += new TrackChange(oldTrackIndex, i, oldTrackList(i));
						println(list.apply(list.size - 1));
					}
		}
		return list.toList
	}

	private def makeTrack(node: Node,arr: Array[Track]): Track = {
			val str: String = (node \ "title").toString()
					val trackStringArr: Array[String] = str.replaceAll("</?title>","").split("[:,]")
					val track: Track = new Track(trackStringArr(1).trim(), trackStringArr(2).trim())
					arr(trackStringArr(0).toInt) = track
					return track
	}
}

class Track(title: String, artist: String) {
	def getTitle = title;
	def getArtist = artist;
	override def toString()  =
			title + ", " + artist;
	override def equals(track: Any): Boolean = { 
			val opt = Option(track)
					return opt.isDefined && this.artist.equals(track.asInstanceOf[Track].getArtist) && this.title.equals(track.asInstanceOf[Track].getTitle) 
	}
}

case class TrackChange(oldPosition: Int, newPosition: Int,track: Track) {
//	def getOldPosition = oldPosition;
//	def getNewPosition = newPosition;
//	def getTrack = track;
//	def getPositionChange = oldPosition - newPosition;
//	override def toString() = 
//	  "Old: " + oldPosition + " New: " + newPosition + " - " + track;
}