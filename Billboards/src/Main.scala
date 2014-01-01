
object Main {
	final val urlString = "http://www1.billboard.com/rss/charts/hot-100";
	def main(args: Array[String]) {
		val billboards = new BillboardsHot100(urlString);
		
		val thread = new DelayedLoop(86400,billboards.checkForChanges);
		thread.run();
	}
}