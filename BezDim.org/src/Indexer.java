import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.List;

public class Indexer {
    private IndexWriter index;
    private StandardAnalyzer analyzer;

    private void initilize() throws IOException {
        this.analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(this.analyzer);

        this.index = new IndexWriter(index, config);
    }

    public void buildIndex(List<Entry> entries) throws IOException {
        initilize();

        for (Entry entry : entries) {
            addDoc(entry);
        }
    }

    private void addDoc(Entry entry)  throws IOException {
        Document doc = new Document();

        TextField field = (new TextField(Constants.INDEX_FIELD_TITLE, entry.getName(), Field.Store.YES));
        field.setBoost(1.5f);

        doc.add(field);
        doc.add(new TextField(Constants.INDEX_FIELD_TYPE, entry.getType(), Field.Store.YES));
        doc.add(new StringField(Constants.INDEX_FIELD_DATE, entry.getDate(), Field.Store.YES));
        doc.add(new TextField(Constants.INDEX_FIELD_LOCATION, entry.getAddress(), Field.Store.YES));

        this.index.addDocument(doc);
    }

    public String[] search(String query, int hitsPerPage) {
        //build the queries
        String modQuery = "";

        for(String split : query.split(" ")) {
            modQuery += split + "~0.8 ";
        }

        Query parsedQuery;

        try {
            parsedQuery = new MultiFieldQueryParser(
                    new String[]{Constants.INDEX_FIELD_TITLE, Constants.INDEX_FIELD_LOCATION},
                    analyzer).parse(modQuery);
        }
        catch(ParseException e) {
            return new String[]{ Constants.INDEX_EXCEPTION_PARSE };
        }

        IndexReader reader;
        IndexSearcher searcher;
        TopDocs docs;
        try {
            reader = DirectoryReader.open(index); //this
            searcher = new IndexSearcher(reader);
            docs = searcher.search(parsedQuery, hitsPerPage); //and this
        }
        catch(IOException e) {
            return new String[]{ Constants.INDEX_EXCEPTION_IO };
        }

        ScoreDoc[] hits = docs.scoreDocs;

        if(hits.length == 0) {
            return new String[]{ Constants.RESULT_NOT_FOUND };
        }

        String result = new String();

        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d;

            try {
                d = searcher.doc(docId);
            } catch (IOException e) {
                return new String[]{"Wild IOException appeared! A document could not be found!"};
            }

            result += (i + 1) + "."
                    + " " + d.get(Constants.INDEX_FIELD_TYPE)
                    + " " + d.get(Constants.INDEX_FIELD_TITLE)
                    + " " + d.get(Constants.INDEX_FIELD_DATE)
                    + " " + d.get(Constants.INDEX_FIELD_LOCATION)
                    + "\n";
        }

        return result.split("\n");
    }
}
