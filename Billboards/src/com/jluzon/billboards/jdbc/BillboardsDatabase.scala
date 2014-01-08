package com.jluzon.billboards.jdbc

import java.sql.DriverManager
import java.sql.ResultSet
import com.jluzon.billboards.jdbc.dto.Email
import scala.collection.mutable.ListBuffer
import com.jluzon.billboards.objects.Track
import com.jluzon.billboards.credentials.DatabaseInfo
import com.jluzon.billboards.objects.TrackListing
import java.sql.Timestamp


object BillboardsDatabase {
	val CONNECTION_STRING = "jdbc:mysql://"+DatabaseInfo.address+":"+DatabaseInfo.port+"/"+DatabaseInfo.databaseName+"?user="+DatabaseInfo.username+"&password="+DatabaseInfo.password;
	val connection = DriverManager.getConnection(CONNECTION_STRING);

	def getEmails(): List[Email] = {
			val list = new ListBuffer[Email]();
			try {
				val statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				val rs = statement.executeQuery("SELECT * FROM emails");

				while(rs.next()) {

					list += Email(rs.getInt("email_id"), rs.getString("email_address"), rs.getBoolean("active"), rs.getTimestamp("signup_date"));
				}
				list.toList;

			}
			finally {
				connection.close()
			}
	}

	def getTrackId(track: Track): Int = {
			try {
				val statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				val rs = statement.executeQuery("SELECT * FROM tracks t WHERE t.artist = \""+ track.artist +"\" AND t.title =\"" + track.title + '\"');

				if(rs.last()) {
					if(rs.getRow() > 1) {
						//error
					}
					return rs.getInt("track_id");
				}
				return -1;				

			}
			finally {
				//connection.close()
			}
	}

	def addTrack(track: Track): Boolean =  {
			if(!Option(track).isDefined)
				return false;
			var successful = false;
			try {

				if(getTrackId(track) == -1) {
					val statement = connection.prepareStatement("INSERT INTO `tracks`(`title`, `artist`) VALUES (?, ?)");
					statement.setString(1, track.title);
					statement.setString(2, track.artist);
					statement.execute();
					true;
				}
				else false;
				//successful;
			}
			finally {
				//connection.close()
				//return successful;
			}
	}
	def addTrackListing(trackListing: TrackListing): Boolean = {
	  if(!Option(trackListing).isDefined)
				return false;
			try {
				var trackId:Int = getTrackId(trackListing.track);
			if( trackId == -1) {
				addTrack(trackListing.track);
				trackId = getTrackId(trackListing.track);
			}
			val trackListingId = getTrackListingId(trackListing);
			if(trackListingId > 0) {
			  return false;
			}
			val statement = connection.prepareStatement("INSERT INTO `listings`(`track_id`, `date`, `position`) VALUES (?,?,?)");
			statement.setInt(1, trackId);
			statement.setTimestamp(2,new Timestamp(trackListing.date.getTime()));
			statement.setInt(3, trackListing.position);
			statement.execute();
			true;
			//successful;
			}
			finally {
				//connection.close()
				//return successful;

			}
	}
	def getTrackListingId(trackListing: TrackListing): Int = {
			try {
				val trackId = getTrackId(trackListing.track);
				if(trackId > 0) {

					val statement = connection.prepareStatement("SELECT * FROM listings l WHERE l.track_id = ? AND l.date = ? AND l.position = ? ");
					statement.setInt(1, trackId);
					statement.setTimestamp(2,new java.sql.Timestamp(trackListing.date.getTime()));
					statement.setInt(3, trackListing.position);
					statement.execute();
					val rs = statement.getResultSet();
					if(rs.last()) {
						if(rs.getRow() > 1) {
							//error
						}
						return rs.getInt("track_id");
					}
				}
				return -1;		
			}
			finally {
				//connection.close()
			}
	}
	def getLastListings():List[TrackListing] = {
	  val list = new ListBuffer[TrackListing]();
	 val statement = connection.prepareStatement( "SELECT l.position, t.title, t.artist, l.date FROM billboards.listings l, billboards.tracks t WHERE l.date = (SELECT MAX(date) FROM billboards.listings) AND l.track_id = t.track_id");
	 val rs = statement.executeQuery();
	 while(rs.next()) {
	   list.append(TrackListing(rs.getInt("position"), Track(rs.getString("title"),rs.getString("artist")), new java.sql.Date(rs.getTimestamp("date").getTime() ) ) );
	 }
	 list.toList;
	}
	
	def getLatestListingDate(): java.sql.Date = {
	  val statement = connection.prepareStatement( "SELECT MAX(date) date FROM listings");
	 val rs = statement.executeQuery();
	 if(rs.next()) {
	   val date = rs.getTimestamp("date")
	   if(Option(date).isDefined)
	   new java.sql.Date(date.getTime());
	   else new java.sql.Date(0L);
	 }
	 else new java.sql.Date(0L);
	}
}