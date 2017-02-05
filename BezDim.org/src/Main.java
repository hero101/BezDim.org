import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Main {
    public static void main(String[] args) throws Exception {
        //for the sake of the project
        //web page retrieval is made with a web crawler
        //crawl();
       new Frame().setVisible(true);
    }

    public static void crawl() throws Exception {
        String crawlStorageFolder = "/data/crawl/root";
        final int numberOfCrawlers = 20;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("http://bezdim.org/signali/reports/");

       controller.start(MyCrawler.class, numberOfCrawlers);
    }
}
