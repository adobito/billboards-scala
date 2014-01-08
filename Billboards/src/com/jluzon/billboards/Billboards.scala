package com.jluzon.billboards

import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Scanner

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML

import com.github.sendgrid.SendGrid
import com.jluzon.billboards.credentials.SendGridCredentials
import com.jluzon.billboards.jdbc.BillboardsDatabase
import com.jluzon.billboards.logger.Logger
import com.jluzon.billboards.objects.PathFinder
import com.jluzon.billboards.objects.Track
import com.jluzon.billboards.objects.TrackListing



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
		Logger.debug("Getting track list from XML feed.");
		val currListings: List[TrackListing] = getTrackListFromURLString(urlString);;
		if(currListings.size != 100) {
		  Logger.error("Error Obtaining Hot 100 List, Expecting 100 listings, obtained " + currListings.size);
		}
		else {
		  Logger.debug("Obtained track list from XML feed successfully.")
		}
		Logger.debug("Checking if new listings found.")
		val date = BillboardsDatabase.getLatestListingDate()
		if(!currListings(0).date.equals(BillboardsDatabase.getLatestListingDate())) {
			Logger.info("Found new listings.")
			Logger.debug("Getting previous listings for comparison.")
			val lastListings = BillboardsDatabase.getLastListings();
			Logger.debug("Obtained previous listings succesfully.")
			currListings.foreach(x => BillboardsDatabase.addTrackListing(x));
			val changeList = compareTrackListings(lastListings, currListings);
			Logger.info("Found " + changeList.size + " new tracks");
			if(!changeList.isEmpty) {
				sendResults(changeList);
			}
		}
		//serializeTrackList(trackList.toList, oldTrackListFile)
		Logger.info("Application shutting down.");
	}
	/**
	 * Gets the XML RSS Feed from the specified String URL and converts it into an Array of TracksListings, in order of position (0 = null)
	 */
	private def getTrackListFromURLString(url: String): List[TrackListing] = {
			try {
				val list = new ListBuffer[TrackListing]();
				val billboardsURL = new URL(urlString);
				val xmlString = Source.fromInputStream(billboardsURL.openStream()).getLines().mkString("\n");
				val xml = XML.load(billboardsURL.openStream());
				val items: NodeSeq = (((xml \ "channel") \ "item"));
				items.foreach(item => list.append(makeTrackListing(item)));
				return list.toList;
			} catch {
				case e: Exception => {Logger.error(e.getStackTrace().mkString("\n")); null;}
			}
	}
	/*
	 * Turns results into a strings and sends them to e-mail list trough SendGrid.
	 */
	private def sendResults(results: List[TrackListing]) {
		val stringBuilder = new StringBuilder();
		results.foreach(trackChange => stringBuilder.append(toHtmlLink(
		        trackChange, YoutubeVideoLinkFinder.getFirstUrlOfSearchTerm(trackChange.track.toString)) + "\n"));
		val resultsString = stringBuilder.toString(); 
		val emailList = getEmailList();
		emailList.foreach(email => sendEmailTo(email, resultsString));
	}
	private def filterResults(changes: List[TrackListing]): List[TrackListing] = {
			changes.filter(track => true);//track.oldPosition == (-1));
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
	  val emails = BillboardsDatabase.getEmails().filter(email => email.active);
	  val list = new ListBuffer[String]();
	  emails.foreach(email => list.append(email.emailAddress));
	  list.toList;
	}
	/**
	 * Compares two track lists and returns the differences
	 */
	private def compareTrackListings(oldTrackListings: List[TrackListing], newTrackListings: List[TrackListing]): List[TrackListing] = {
		val list: ListBuffer[TrackListing] = new ListBuffer[TrackListing]();
		newTrackListings.filter(newListing => oldTrackListings.filter(oldListing => newListing.track.equals(oldListing.track) ).isEmpty );
	}
	/**
	 * Makes track object from XML Node and adds it to array.
	 */
	private def makeTrackListing(node: Node): TrackListing = {
		val title = (node \ "title");;
		val pubDate = node \ "pubDate";
		val date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US).parse(pubDate.text); 
		val trackStringArr: Array[String] = title.text.split("[:,]");
		val track: Track =  Track(trackStringArr(1).replace("&amp;", "&").trim(), trackStringArr(2).replace("&amp;", "&").trim());
		val trackListing: TrackListing = TrackListing(trackStringArr(0).toInt, track, new java.sql.Date(date.getTime()));
		return trackListing;
	}
	private def toHtmlLink(trackListing: TrackListing, link: String):String = {
	  "<a href=\""+ link + "\" target=\"_blank\" style=\"target-new: tab;\">" + trackListing.position + ". " + trackListing.track.toString() + "</a><br>" 
	}
}

