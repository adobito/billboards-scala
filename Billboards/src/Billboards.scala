import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.util.Scanner
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.pickling._
import scala.pickling.json._
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML
import com.github.sendgrid.SendGrid
import java.util.Date
import java.util.Calendar
import java.util.GregorianCalendar




class BillboardsHot100(urlString: String) {
	final val OLD_TRACK_LIST_PATH = "oldTrackList.txt";
	final val toEmail = "jesus.e.luzon@gmail.com";
	final val EMAILS_PATH = "eMails.txt";
	var oldTrackList: Array[Track] = Array.empty[Track];;

	def checkForChanges() {

		val trackList: Array[Track] = getTrackListFromURLString(urlString);;
		if(oldTrackList.isEmpty) {
			val file = new File(OLD_TRACK_LIST_PATH)
			if(file.exists()) {
				oldTrackList = deserializeTrackList(file).toArray;
			}
		}
		val changeList : List[TrackChange] = compareArrays(oldTrackList, trackList);
		val filteredChangeList = filterResults(changeList);
//		println(filteredChangeList.mkString("\n"));
		if(!changeList.isEmpty) {
		sendResults(filteredChangeList);
		}
		val oldTrackListFile = new File(OLD_TRACK_LIST_PATH);
		serializeTrackList(trackList.toList, oldTrackListFile)
	}
	def serializeTrackList(obj: List[Track], file: File) {
		val pickle = obj.pickle;
//		println(pickle.toString());
		val writer = new PrintWriter(file);
		writer.write(pickle.value);
		writer.flush();
	}

	def deserializeTrackList(file: File): List[Track] = {

			val reader: Scanner = new Scanner(file);
	val builder = new StringBuilder();
	while(reader.hasNextLine()) {
		builder.append(reader.nextLine());
	}
	val pickle = JSONPickle(builder.toString);
	pickle.unpickle[List[Track]];
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
		emailList.foreach(email => sendEmailTo(sendGrid, email, resultsString));
	}
	def filterResults(changes: List[TrackChange]): List[TrackChange] = {
			changes.filter(track => track.oldPosition == -1);
	}
	def sendEmailTo(sendGrid: SendGrid, toEmail: String, message: String) {
		sendGrid.addTo(toEmail);
		
		sendGrid.setSubject("Billboard Hot 100 Changes " + getCalendarDateString());
		sendGrid.setFrom("admin@jluzon.com");
		sendGrid.setFromName("Hot 100 Feed");
		sendGrid.setText(message);
		sendGrid.send();
	}
	
	def getCalendarDateString(): String = {
	  val cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.get(Calendar.MONTH) + 1 + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.YEAR);
	}
	def getEmailList: List[String] = {
			val file = new File(EMAILS_PATH);
			val in = new Scanner(file);
			val emailList = new ListBuffer[String]();
			while(in.hasNextLine()) {
				emailList.append(in.nextLine());
			}
			emailList.toList;
	}
	private def compareArrays(oldTrackList: Array[Track], newTrackList: Array[Track]): List[TrackChange] = {
			val list: ListBuffer[TrackChange] = new ListBuffer[TrackChange]();
	for(i <- 1 to 100) {
		val oldTrackIndex = oldTrackList.indexOf(newTrackList(i));

		if( Option(newTrackList(i)).isDefined &&  oldTrackIndex != i) {
			list += new TrackChange(oldTrackIndex, i, newTrackList(i));
		}
	}
	return list.toList;
	}

	private def makeTrack(node: Node,arr: Array[Track]): Track = {
			val str: String = (node \ "title").toString();;
			val trackStringArr: Array[String] = str.replaceAll("</?title>","").split("[:,]");
			val track: Track = new Track(trackStringArr(1).trim(), trackStringArr(2).trim());
			arr(trackStringArr(0).toInt) = track;
			return track;
	}
}

