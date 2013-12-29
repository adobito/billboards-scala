case class Track(title: String, artist: String) {
	override def toString()  =
			title + ", " + artist;
	override def equals(track: Any): Boolean = { 
			Option(track).isDefined && this.artist.equals(track.asInstanceOf[Track].artist) && this.title.equals(track.asInstanceOf[Track].title) 
	}
}