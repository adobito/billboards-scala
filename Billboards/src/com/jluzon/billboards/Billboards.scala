package com.jluzon.billboards

import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Scanner
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.pickling._
import scala.pickling.json._
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML
import com.github.sendgrid.SendGrid
import com.jluzon.billboards.objects.TrackChange
import com.jluzon.billboards.objects.Track
import com.jluzon.billboards.credentials.SendGridCredentials
import com.jluzon.billboards.logger.Logger
import java.io.FileNotFoundException
import com.jluzon.billboards.objects.PathFinder



class BillboardsHot100(urlString: String) {
	private val PATH = PathFinder.getPath;
	val OLD_TRACK_LIST_FILE_NAME = "oldTrackList.txt";
	val ADMIN_FROM_EMAIL = "admin@jluzon.com";
	val EMAIL_SUBJECT = "Hot 100 Feed";
	val EMAIL_SUBJECT_HEADER_PREFIX = "Billboard Hot 100 Changes ";
	val EMAILS_FILE_NAME = "eMails.txt";
	private var oldTrackList: Array[Track] = Array.empty[Track];
	/**
	 * Main code operation. Checks RSS Feed for changes in tracks and send to the email
	 */
	def run() {
		Logger.info("Application starting up.")
		val trackList: Array[Track] = getTrackListFromURLString(urlString);;
		if(oldTrackList.isEmpty) {
			val file = new File(PATH + OLD_TRACK_LIST_FILE_NAME)
			if(file.exists()) {
				Logger.debug("Old track list found: Attempting to deserialize")
				oldTrackList = deserializeTrackList(file).toArray;
				
			}
			else {
			  Logger.debug("Old track list not found.")
			}
		}
		val changeList : List[TrackChange] = compareTrackLists(oldTrackList, trackList);
		val filteredChangeList = filterResults(changeList);
		Logger.info("Found " + filteredChangeList.size + " new tracks");
		if(!changeList.isEmpty) {
			sendResults(filteredChangeList);
		}
		val oldTrackListFile = new File(PATH + OLD_TRACK_LIST_FILE_NAME);
		Logger.debug("Attempting to Serialize old track list to " + PATH + OLD_TRACK_LIST_FILE_NAME);
		serializeTrackList(trackList.toList, oldTrackListFile)
		Logger.info("Application shutting down.");
	}
	/**
	 * Serializes track list and persists it in a JSON text file for next run.
	 */
	private def serializeTrackList(obj: List[Track], file: File) {
		try {
			val pickle = obj.pickle;
			//		println(pickle.toString());
			val writer = new PrintWriter(file);
			writer.write(pickle.value);
			writer.flush();
			Logger.debug("Serialized " + file.getName() + " successfully.")
		}
		catch {
			case e: Exception => Logger.error("Could not serialize new track list. \n" + e.getStackTrace().mkString("\n"));
		}

	}
	/**
	 * Deserializes old track list stored and turns it back into a List of Tracks, in order.
	 */
	private def deserializeTrackList(file: File): List[Track] = {
			try {
				val reader: Scanner = new Scanner(file);;
				val builder = new StringBuilder();
				while(reader.hasNextLine()) {
					builder.append(reader.nextLine());
				}
				val pickle = JSONPickle(builder.toString);
				val trackList = pickle.unpickle[List[Track]]; 
				Logger.debug(file.getName() + " deserialized succesfully");
				trackList
			} catch {
				case e: Exception =>  { Logger.error(e.getStackTrace().mkString("\n")); List[Track](); }
			}

	}

	/**
	 * Gets the XML RSS Feed from the specified String URL and converts it into an Array of Tracks, in order of position (0 = null)
	 */
	private def getTrackListFromURLString(url: String): Array[Track] = {
			try {
				val billboardsURL = new URL(urlString);
				val xmlString = Source.fromInputStream(billboardsURL.openStream()).getLines().mkString("\n");
				val xml = XML.load(billboardsURL.openStream());
				val items: NodeSeq = (((xml \ "channel") \ "item"));
				val trackList = new Array[Track](101);
				items.foreach(x => (makeTrack(x,trackList)));
				return trackList;
			} catch {
				case e: Exception => {Logger.error(e.getStackTrace().mkString("\n")); null;}
			}
	}
	/*
	 * Turns results into a strings and sends them to e-mail list trough SendGrid.
	 */
	private def sendResults(results: List[TrackChange]) {
		val stringBuilder = new StringBuilder();
		results.foreach(trackChange => stringBuilder.append(toHtmlLink(
		        trackChange, YoutubeVideoLinkFinder.getFirstUrlOfSearchTerm(trackChange.track.toString)) + "\n"));
		val resultsString = stringBuilder.toString(); 
		val emailList = getEmailList();
		emailList.foreach(email => sendEmailTo(email, resultsString));
	}
	private def filterResults(changes: List[TrackChange]): List[TrackChange] = {
			changes.filter(track => track.oldPosition == (-1));
	}
	/**
	 * Sends email through sendgrid.
	 */
	private def sendEmailTo(toEmail: String, message: String) {
		val sendGrid = new SendGrid(SendGridCredentials.username,SendGridCredentials.password);
		sendGrid.addTo(toEmail);
		sendGrid.setSubject(EMAIL_SUBJECT_HEADER_PREFIX + getCalendarDateString());
		sendGrid.setFrom(ADMIN_FROM_EMAIL);
		sendGrid.setFromName(EMAIL_SUBJECT);
		sendGrid.setHtml(message);
		sendGrid.send();
		Logger.info("Email to " + toEmail + " sent successfully");
	}
	
	/**
	 * Gets a calendar date in MM-DD-YYYY format
	 */
	private def getCalendarDateString(): String = {
			val cal = new GregorianCalendar();
			cal.setTime(new Date());
			cal.get(Calendar.MONTH) + 1 + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.YEAR);
	}
	/**
	 * Reads email list from EMAILS_PATH and converts it into a List[String]
	 */
	private def getEmailList(): List[String] = {
			try {
				val file = new File(PATH + EMAILS_FILE_NAME);
				val in = new Scanner(file);
				val emailList = new ListBuffer[String]();
				while(in.hasNextLine()) {
					emailList.append(in.nextLine());
				}
				emailList.toList;
			}
			catch {
				case e: FileNotFoundException => {Logger.error(e.getMessage()); List[String]()}
				case e: Exception => {Logger.error(e.getStackTrace().mkString("\n")); null;}
			}
	}
	/**
	 * Compares two track lists and returns the differences
	 */
	private def compareTrackLists(oldTrackList: Array[Track], newTrackList: Array[Track]): List[TrackChange] = {
		val list: ListBuffer[TrackChange] = new ListBuffer[TrackChange]();
		for(i <- 1 to 100) {
			val oldTrackIndex = oldTrackList.indexOf(newTrackList(i));
			if( Option(newTrackList(i)).isDefined &&  oldTrackIndex != i) {
				list += new TrackChange(oldTrackIndex, i, newTrackList(i));
			}
		}
		return list.toList;
	}
	/**
	 * Makes track object from XML Node and adds it to array.
	 */
	private def makeTrack(node: Node,arr: Array[Track]): Track = {
		val str: String = (node \ "title").toString();;
		val trackStringArr: Array[String] = str.replaceAll("</?title>","").split("[:,]");
		val track: Track = new Track(trackStringArr(1).trim(), trackStringArr(2).trim());
		arr(trackStringArr(0).toInt) = track;
		return track;
	}
	private def toHtmlLink(trackChange: TrackChange, link: String):String = {
	  "<a href=\""+ link + "\" target=\"_blank\" style=\"target-new: tab;\">" + trackChange.newPosition + ". " + trackChange.track.toString() + "</a><br>" 
	}
}

