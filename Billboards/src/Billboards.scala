import java.io.File
import java.io.PrintWriter
import java.net.URL
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML
import com.github.sendgrid.SendGrid
import scala.xml.Null


class BillboardsHot100(urlString: String) {
	final val oldTrackList = new Array[Track](101);
	final val toEmail = "jesus.e.luzon@gmail.com";
	def checkForChanges() {

		val trackList = getTrackListFromURLString(urlString);

		var shuff = Random.shuffle(trackList.toList);
		shuff = shuff.updated(7, new Track("Aloha", "The Hawaiians"));

		val changeList : List[TrackChange] = compareArrays(trackList, shuff.toArray);
		val stringBuilder = new StringBuilder();
		//		println(changeList.mkString("\n"))
		changeList.filter(x => x.oldPosition == -1).foreach(x => stringBuilder ++= x.track + "\n" );

		println(stringBuilder.toString);
		sendResults(filterResults(changeList));

	}
	def getTrackListFromURLString(url: String): Array[Track] = {
			val billboardsURL = new URL(urlString);
			val xmlString = Source.fromInputStream(billboardsURL.openStream()).getLines().mkString("\n");
			val xml = XML.load(billboardsURL.openStream());
			val items: NodeSeq = (((xml \ "channel") \ "item"));

			val trackList = new Array[Track](101);
			items.foreach(x => (makeTrack(x,trackList)));
			return trackList;
	}
	def sendResults(results: List[TrackChange]) {
		val resultsString = results.mkString("\n");
		val emailList = getEmailList;
		val sendGrid = new SendGrid(SendGridCredentials.username,SendGridCredentials.password);
		// emailList.foreach(email => sendEmailTo(sendGrid, email, resultsString));	  
	}
	def filterResults(changes: List[TrackChange]): List[TrackChange] = {
			changes.filter(track => track.oldPosition == -1);
	}
	def sendEmailTo(sendGrid: SendGrid, toEmail: String, message: String) {
		sendGrid.addTo(toEmail);
		sendGrid.setSubject("Billboard Hot 100 Changes");
		sendGrid.setFrom("admin@jluzon.com");
		sendGrid.setFromName("Hot 100 Feed");
		sendGrid.setText(message);
		sendGrid.send();
	}
	def getEmailList : List[String] = {
			val emailList = Array(toEmail);
			emailList.toList
	}
	private def compareArrays(oldTrackList: Array[Track], newTrackList: Array[Track]): List[TrackChange] = {
			val list: ListBuffer[TrackChange] = new ListBuffer[TrackChange]();
	//val track = new Track("","") //oldTrackList(i)
	for(i <- 1 to 100) {
		val oldTrackIndex = oldTrackList.indexOf(newTrackList(i))

				if( Option(newTrackList(i)).isDefined &&  oldTrackIndex != i) {
					list += new TrackChange(oldTrackIndex, i, newTrackList(i));
					//println(list.last);
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




