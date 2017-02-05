import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/* адрес и дата винаги има
 * в случай, че името не е открито
 * възстанови по адрес
 * в случай, че вида не е открит
 * възтанови по адрес
 */
public class MyCrawler extends WebCrawler {

	private final static Pattern FILTERS = 
			Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?" + 
					"|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
					"|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	
	private final static String[] PLACE_TYPES = {
			"бар",
			"клуб",
			"ресторант",
			"бирария",
			"механа",
			"кръчма",
			"дискотека",
			"заведение",
			"пицария",
			"кафе",
			"кафене",
			"бистро"
	};
	
	private final static int MAX_ENTRIES = 200;
	
	private List<Entry> entryArray = new ArrayList<>();
	
	private boolean isFinished = false;

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches()
				&& href.startsWith(Constants.DOMAIN);
	}

	@Override
	public void visit(Page page) {
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText().toLowerCase();

			if((text.contains("потвърден") || text.contains("verified"))
					&& text.contains("заведение за хранене и развлечение")) {
				String html = htmlParseData.getHtml();
				
				String placeName;;
                String typeName = "N/A";
				String date = "N/A";
				String location = "N/A";

				Document doc = Jsoup.parse(html);
				
				Elements spans = doc.getElementsByTag(Constants.TAG);
                //get location and date
				for(Element el : spans) {
					if(el.attr("class").equals(Constants.DATECLASS)) {
						date = el.text();
					}
					else if(el.attr("class").equals(Constants.LOCATIONCLASS)) {
						location = el.text();
					}
				}
				
				if(date.equals("N/A") || location.equals("N/A")) {
					return;
				}

				if(!location.toLowerCase().contains("софия")) {
					return;
				}
				
				String title = htmlParseData.getTitle();
				
				int typeFoundAt = 0;
				
				for(String placeType : PLACE_TYPES) {
					typeFoundAt = title.indexOf(placeType);
					
					if(typeFoundAt != -1) {
						typeFoundAt += placeType.length() + 1; //+ 1 white space
						typeName = placeType;
						break;
					}
				}
				
				if(typeFoundAt != -1) {
					int stopFoundAt = title.indexOf(',');
					
					if(stopFoundAt != -1) {
						placeName = title.substring(
							typeFoundAt, stopFoundAt);
					}
					else {
						placeName = "N/A";
					}
				}
				else {
					placeName = "N/A";
				}

                placeName = placeName.replaceAll("\"", "").replaceAll("'", "").replaceAll("”", "").replaceAll("“", "").replaceAll("„", "");
                date = date.replaceAll("\"", "").replaceAll("'", "").replaceAll("”", "").replaceAll("“", "").replaceAll("„", "");
                location = location.replaceAll("\"", "").replaceAll("'", "").replaceAll("”", "").replaceAll("“", "").replaceAll("„", "").replace("№", "");
				
				Entry entry = new Entry(typeName, placeName, date, location);

				System.out.println(entry.toString());
				
				entryArray.add(entry);

                //write to file
                //create if it doesnt exist and append if it exists
				try {
                    Files.write(Paths.get(Constants.ENTRIES), (entry.toString() + "\n").getBytes(),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}