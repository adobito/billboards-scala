object Main {
	final val toEmail = "jesus.e.luzon@gmail.com";
	final val urlString = "http://www1.billboard.com/rss/charts/hot-100";
	def main(args: Array[String]) {
		val billboards = new BillboardsHot100(urlString);
		billboards.checkForChanges();
	}
}