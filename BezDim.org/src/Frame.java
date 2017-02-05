import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.ScrollableSizeHint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame extends JXFrame {
    private Indexer indexer;

    private JPanel paneSearch;

    private JTextField txtSearchBar;
    private JButton btnSearch;

    //container for past search results
    private JXTaskPaneContainer taskPaneContainer;
    //save old results to add new result on top
    private List<JXTaskPane> taskPaneList;


    public Frame() {
        //prepare the index
        initData();

        //launch the UI
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.setTitle(Constants.UI_TITLE);

        this.setSize(Constants.UI_DEFAULT_WIDTH, Constants.UI_DEFAULT_HEIGHT);
        this.setMinimumSize(new Dimension(Constants.UI_MIN_WIDTH, Constants.UI_MIN_HEIGHT));
        //window centered
        this.setLocationRelativeTo(null);

        this.paneSearch = new JPanel();
        {
            this.btnSearch = new JButton(new AbstractAction(Constants.UI_BTN_TITLE) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    executeQuery();
                }
            });

            this.txtSearchBar = new JTextField(20);

            this.txtSearchBar.addActionListener(this.btnSearch.getActionListeners()[0]);

            this.paneSearch.add(this.txtSearchBar);
            this.paneSearch.add(this.btnSearch);
        }
        this.add(this.paneSearch, BorderLayout.NORTH);

        //list of task panes for re-display
        this.taskPaneList = new ArrayList<>();

        this.taskPaneContainer = new JXTaskPaneContainer();
        //enable horizontal scroll
        this.taskPaneContainer.setScrollableWidthHint(ScrollableSizeHint.HORIZONTAL_STRETCH);
        this.add(new JScrollPane(taskPaneContainer), BorderLayout.CENTER);
    }

    private void executeQuery() {
        long timer = System.nanoTime();

        String query = txtSearchBar.getText()
                //remove white spaces
                .trim()
                //remove special query symbols
                .replaceAll("~", "")
                .replaceAll("!", "");

        if(query.length() == 0) {
            return;
        }

        String[] results = indexer.search(query, 20);
        addSearchResult(query, results, (System.nanoTime() - timer) / (1000.0 * 1000.0 * 1000.0)); //nano 10e-9
    }

    public void addSearchResult(String searchQuery, String[] results, double executeTime) {
        JXTaskPane taskPane = new JXTaskPane();
        taskPane.setCollapsed(true);

        if(results[0].equals(Constants.RESULT_NOT_FOUND)) {
            taskPane.setTitle("\"" + searchQuery + "\"" + " - 0 results (" + executeTime + " seconds)");
        }
        else {
            taskPane.setTitle("\"" + searchQuery + "\"" + " - " + results.length + " result" + (results.length > 1 ? "s" : "") + " (" + executeTime + " seconds)");

            for (int i = 0; i < results.length; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.add(new JLabel(results[i]));
                taskPane.add(row);
            }
        }

        this.taskPaneList.add(0, taskPane);
        this.taskPaneContainer.removeAll();

        for(JXTaskPane pane : this.taskPaneList) {
            this.taskPaneContainer.add(pane);
        }
        //display the taskPane
        this.taskPaneContainer.revalidate();
    }

   private void initData() {
       //get crawled entries
       List<Entry> entries = null;
       try {
          entries = readCrawledEntries();
       }
       catch(IOException e) {
           System.err.println("Crawled entries file not found!");
           System.exit(1);
       }

       //fix entries data
       //remove duplicates and attempt to recover missing slots
       entries = fixData(entries);

       //build index
       this.indexer = new Indexer();
       try {
           indexer.buildIndex(entries);
       }
       catch(IOException e) {
           System.err.println("IOException while building index!");
           System.exit(1);
       }
   }

    public static List<Entry> readCrawledEntries() throws IOException {
        List<Entry> entries = new ArrayList<>();

        List<String> lines = Files.readAllLines(Paths.get(Constants.ENTRIES));

        for(String line : lines) {
            String placeName = line.substring(6, line.indexOf(" Type"));
            String typeName = line.substring(line.indexOf(" Type: ") + 7, line.indexOf(" Date"));
            String date = line.substring(line.indexOf(" Date: ") + 7, line.indexOf(" Address"));
            String location = line.substring(line.indexOf(" Address: ") + 10);

            entries.add(new Entry(typeName, placeName, date, location));
        }

        return entries;
    }

    public static List<Entry> fixData(List<Entry> entries) {
        final String[] PLACE_TYPES = {
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
        //make a golden entry set with entries with no missing slots
        //and remove duplicate entries by location
        //because missing slots have same values Constants.BLANK
        Map<String, Entry> goldenEntryMap = new HashMap<>();

        for(Entry entry : entries) {
            if(!entry.getName().equals(Constants.BLANK) && !entry.getType().equals(Constants.BLANK)) {
                goldenEntryMap.put(entry.getAddress(), entry);
            }
        }
        //map to list
        //  List<Entry> fixedEntryList = new ArrayList<>(goldenEntryMap.values());

        List<Entry> fixedEntryList = new ArrayList<>();

        for(Entry entry : entries) {
            if(entry.getName().equals(Constants.BLANK)) {
                //check if a golden entry with the same address exists
                Entry goldenEntry = goldenEntryMap.get(entry.getAddress());
                //if it exist - recover name and type
                if (goldenEntry != null) {
                    entry.setName(goldenEntry.getName());
                }
            }

            if(entry.getAddress().equals(Constants.BLANK)) {
                //check if a golden entry with the same address exists
                Entry goldenEntry = goldenEntryMap.get(entry.getAddress());
                //if it exist - recover name and type
                if (goldenEntry != null) {
                    entry.setType(goldenEntry.getType());
                }
            }

            //if entry's name isnt recovered exclude it
            if(entry.getName().equals(Constants.BLANK)) {
                continue;
            }

            fixedEntryList.add(entry);
        }
        return fixedEntryList;
    }
}