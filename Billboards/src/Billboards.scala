import java.net.URL

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML

object Main {
	def main(args: Array[String]) {
		val billboardsURLString : String = "http://www1.billboard.com/rss/charts/hot-100" 
				val billboardsURL = new URL(billboardsURLString)
	val xmlString = Source.fromInputStream(billboardsURL.openStream()).getLines().mkString("\n")
	val xml = XML.load(billboardsURL.openStream())
	//println( (xml \ "channel").mkString("\n") );
	//	println((((xml \ "channel") \ "item")).mkString("\n"));
	val items: NodeSeq = (((xml \ "channel") \ "item"))
	//val trackList = new ListBuffer[Track]();
	//	val NodeSeq = NodeSeq(xml); 
	val trackList = new Array[Track](101)
	items.foreach(x => (makeTrack(x,trackList)))

	val track = new Track("The Monster","Eminem Featuring Rihanna")
		val track2 = new Track("Roar","Katy Perry")

	//		trackList.foreach(x => println(x))
	def equalsNotNull(first: Track, second: Track):Boolean = {
		val opt = Option(first)
		val opt2 = Option(second)
				return opt.isDefined && opt2.isDefined && first.equals(second) 
	}
	println(trackList.filter(x =>  equalsNotNull(x,track2)).mkString(""))
	println(trackList.indexWhere(x => equalsNotNull(x,track2)))
	}

	def compareArrays(oldTrackList: Array[Track], newTrackList: Array[Track]) {
	  for(i <- 1 to 101) {
	    if(newTrackList.indexOf(oldTrackList(i)) != i) {
	      
	    }
	  }
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
	override def equals(track: Any): Boolean = { return this.artist.equals(track.asInstanceOf[Track].getArtist) && this.title.equals(track.asInstanceOf[Track].getTitle) }
}
