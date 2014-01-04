package com.jluzon.billboards;
import java.io.IOException;
import java.net.URL;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;



public class YoutubeVideoLinkFinder {

	public static String getFirstUrlOfSearchTerm(String searchTerm) throws IOException, ServiceException {
		YouTubeService service = new YouTubeService("Billboards Hot 100 Change Newsletter");
		YouTubeQuery query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
		query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);

		query.setFullTextQuery(searchTerm);

		VideoFeed videoFeed = service.query(query, VideoFeed.class);
		return videoFeed.getEntries().get(0).getMediaGroup().getPlayer().getUrl();
	}

}
