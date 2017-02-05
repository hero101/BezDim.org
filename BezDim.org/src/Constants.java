/**
 * Created by User on 25/1/2017.
 */
public final class Constants {
    //domain for crawling
    public final static String DOMAIN = "https://bezdim.org/signali/reports";
    //file path for saving crawled entries
    public final static String ENTRIES = "BezDim.org/entries.txt";
    //crawler specific constants
    public final static String TAG = "span";
    public final static String DATECLASS = "r_date";
    public final static String LOCATIONCLASS = "r_location";
    //value for unknown entry
    public final static String BLANK = "N/A";

    //indexing field types
    public final static String INDEX_FIELD_TITLE = "name";
    public final static String INDEX_FIELD_TYPE = "type";
    public final static String INDEX_FIELD_DATE = "date";
    public final static String INDEX_FIELD_LOCATION = "location";

    public final static String INDEX_EXCEPTION_PARSE = "Wild ParseException appeared while processing your query!";
    public final static String INDEX_EXCEPTION_IO = "Wild IOException appeared! Index could not be found!";



    public final static String RESULT_NOT_FOUND = "No Results!";

    //UI size
    public final static int UI_DEFAULT_WIDTH = 1000;
    public final static int UI_DEFAULT_HEIGHT = 500;
    public final static int UI_MIN_WIDTH = 400;
    public final static int UI_MIN_HEIGHT = 500;
    //UI title
    public final static String UI_TITLE = "BezDim.org Search Engine";
    //UI components text
    public final static String UI_BTN_TITLE = "Search";
}
