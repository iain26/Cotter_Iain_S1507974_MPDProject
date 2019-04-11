// Name                 Iain Cotter
// Student ID           S1507974
// Programme of Study   Computer Games (Software Development)
//

package me.gcu.cotter_s1507974;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Task extends Fragment {

    // types of sorts and searches that can be undertaken as well as current selections
    public enum typesToSort {DATE, TIME, LOCATION, CATEGORY, MAGNITUDE, DEPTH, NORTHERN, EASTERN}
    public typesToSort sort = typesToSort.DATE;
    public enum searchType {DATE, LOCATION, DAY}
    public searchType currentSearch = searchType.DATE;
    public searchType lastSearch = searchType.DATE;

    // searching and sorting settings
    public boolean searching = false;
    public boolean asc = false;

    private String searchInput = "";

    // lists that hold ListData class for storing information parsed from URl
    private ArrayList<ListData> originalList = new ArrayList<ListData>();
    // List to be passed to activity to update List View component
    public ArrayList<ListData> displayList = new ArrayList<ListData>();
    // list that holds search items
    public ArrayList<ListData> searchList = new ArrayList<ListData>();

    // url source to load xml from
    private String url = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";

    // time to update list in seconds
    private int timeInSeconds = 120;

    interface Callbacks {
        void onDisplayUpdate(String[] update);
    }
    private Callbacks callbacks;

    // when activity changes
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    //when fragment is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // is not destroyed on activity change
        setRetainInstance(true);

        // Start AsyncTask to load URL and parse the data
        // works on a timer to automatically update values
        ReadParse readParse = new ReadParse();
        readParse.execute(timeInSeconds);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    // passed to from activity so that when activity list update occurs keep the passed in string
    public void SearchList(String input) {
        // boolean to check if search is being undertaken
        searching = true;
        searchInput = input;
        // update list
        OrderList();
    }

    // get the list a search returns
    public ArrayList<ListData> GetListSearch(String input){
        ArrayList<ListData> temp = new ArrayList<ListData>();

        for (ListData data : originalList) {
            boolean searchItem = true;
            String[] sInput = input.split(" ");

            for (String seperateChunks : sInput) {
                seperateChunks.trim();
                String sCheck = "";
                if (!seperateChunks.matches("")) {
                    switch (currentSearch) {
                        case DATE:
                            if (seperateChunks.length() < 3) {
                                seperateChunks += " ";
                            }
                            sCheck = data.date.substring(6);
                            if (!sCheck.contains(seperateChunks)) {
                                searchItem = false;
                            }
                            break;
                        case LOCATION:
                            if (!data.location.contains(seperateChunks.toUpperCase())) {
                                searchItem = false;
                            }
                            break;
                        case DAY:
                            sCheck = data.date.substring(0, 5);
                            sCheck.trim();
                            if (!sCheck.contains(seperateChunks)) {
                                searchItem = false;
                            }
                            break;
                    }
                }
            }

            if (searchItem == true) {
                if (!temp.contains(data)) {
                    temp.add(data);
                }
            }
        }
        return temp;
    }

    // displayed list that appears on List View is sorted
    public void OrderList() {

        //reset list to display
        displayList.clear();

        // get display list from list originally parsed from the URL and if user searching
        // then only add search items
        searchList = GetListSearch(searchInput);
        if (searching == false) {
            for (ListData data : originalList) {
                displayList.add(data);
            }
        } else {
            for (ListData data : originalList) {
                if (searchList.contains(data)) {
                    displayList.add(data);
                }
            }
        }

        // depending on sort from high to low
        // date is originally loaded most recent so doesnt need to be sorted
        switch (sort) {
            case TIME:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return o2.time.compareTo(o1.time);
                    }
                });
                break;
            case LOCATION:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return o2.location.compareTo(o1.location);
                    }
                });
                break;
            case CATEGORY:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return o2.category.compareTo(o1.category);
                    }
                });
                break;
            case MAGNITUDE:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o2.magnitude, o1.magnitude);
                    }
                });
                break;
            case DEPTH:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o2.depth, o1.depth);
                    }
                });
                break;
            case NORTHERN:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o2.coordinates[0], o1.coordinates[0]);
                    }
                });
                break;
            case EASTERN:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o2.coordinates[1], o1.coordinates[1]);
                    }
                });
                break;
        }

        // if user toggled switch on then invert list
        if (asc == true) {
            Collections.reverse(displayList);
        }

        // string list to get information to display in item of List View
        ArrayList<String> temp = new ArrayList<String>();
        for (ListData displayData : displayList) {
            temp.add(displayData.textFormat);
        }

        // if search was undertaken and nothing to display then display that to user
        if (searching == true) {
            if (temp.size() == 0) {
                temp.add("The Search Produced No Results...");
            }
        }

        // call to activity to change List View component
        String[] arr = temp.toArray(new String[temp.size()]);
        callbacks.onDisplayUpdate(arr);
    }

    // change originally list that holds parsed data from URL and sends call to sort display List
    public void AddToList(ArrayList<String> list) {
        originalList.clear();
        for (String listItem : list) {
            // ListData instance is created and assigned values from string
            ListData temp = new ListData(listItem);
            originalList.add(temp);
        }

        OrderList();
    }

    // AsyncTask that works on a seperate thread to improve performance
    private class ReadParse extends AsyncTask<Integer, String, String> {
        ArrayList<String> list = new ArrayList<String>();

        // on background is in a while loop that never ends
        @Override
        protected String doInBackground(Integer... params) {
            boolean run = true;

            while (run){
                try
                {
                    // Url is read and xml is passed to document
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(new URL(url).openStream());
                    // document is then read to string and placed on single line for parsing
                    String result = DocumentToString(doc);
                    result = result.replaceAll("\n", "");

                    // the string data is passed to parser and returns list with strings
                    ParseXml parseXml = new ParseXml();
                    list = parseXml.getString(result);

                    // event triggered to call onProgressUpdate
                    publishProgress("");
                    // stops the thread and subsequently while loop for the amount of seconds specified
                    Thread.sleep( params[0]*1000 );

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // inform user that new list was read from URL and update old original list
            // runs on the main thread
            Toast.makeText(getActivity(),"LIST UPDATED!!!", Toast.LENGTH_SHORT).show();
            AddToList(list);
        }


        public void ElementToStream(Element element, OutputStream out) {
            try {
                DOMSource src = new DOMSource(element);
                StreamResult result = new StreamResult(out);
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                transformer.transform(src, result);
            } catch (Exception ex) {
            }
        }

        public String DocumentToString(Document doc) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ElementToStream(doc.getDocumentElement(), byteArrayOutputStream);
            return new String(byteArrayOutputStream.toByteArray());
        }
    }

    // class to read
    public class ParseXml {

        // xml tags that soecify what information to locate in xml
        private String itemTag = "item";
        private String descTag = "description";
        private String dateTag = "pubDate";
        private String catTag = "category";
        private String latTag = "lat";
        private String longTag = "long";

        public ArrayList<String> getString(String sData) throws IOException {

            ArrayList<String> outputs = new ArrayList<String>();
            try {
                // reads the xml
                XmlPullParserFactory xfo = XmlPullParserFactory.newInstance();
                xfo.setNamespaceAware(true);
                XmlPullParser p = xfo.newPullParser();

                p.setInput(new StringReader(sData));
                int evt = p.getEventType();

                String tag = "";
                boolean record = false;
                String output = "";

                while (evt != XmlPullParser.END_DOCUMENT) {
                    switch (evt) {
                        case XmlPullParser.START_TAG:
                            tag = p.getName();
                            if(itemTag.equals(tag)){
                                // if tag is specific to a useful element (i.e earthquake data) then start recording data
                                record = true;
                            }
                            break;

                        case XmlPullParser.END_TAG:
                            tag = p.getName();
                            if(itemTag.equals(tag)){
                                // if tag ending is useful element (i.e earthquake data) then stop recording data
                                record = false;
                                // add data to list and reset this output string
                                outputs.add(output);
                                output = "";
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if(record == true) {
                                if (descTag.equals(tag) || dateTag.equals(tag) || catTag.equals(tag) || latTag.equals(tag) || longTag.equals(tag)) {
                                    // if tag is equal to specific elements while recording then add data to output string
                                    // use semi-colon to separate data
                                    output += " ; " + p.getText();
                                }
                            }
                            break;
                    }
                    evt = p.next();
                }

            }catch(Exception ex){
                ex.printStackTrace();
            }
            // return final list of output strings
            return outputs;
        }
    }

    // class to hold earthquake data
    public class ListData {
        String location = "";
        String date = "";
        String time = "";
        String category = "";
        float magnitude = 0f;
        float depth = 0f;
        float[] coordinates = {0f, 0f};
        String textFormat;
        String infoFormat;

        public ListData(String text) {
            // seperate string by semicolon
            String[] segments = text.split(";");

            // get data from specific chunk of string
            location = segments[2].substring(10);
            date = segments[6].substring(0, segments[6].length() - 9);
            time = segments[6].substring(segments[6].length() - 9);
            category = segments[7];
            magnitude = Float.parseFloat(segments[5].substring(12));
            depth = Float.parseFloat(segments[4].substring(8, segments[4].length() - 3));
            coordinates[0] = Float.parseFloat(segments[8]);
            coordinates[1] = Float.parseFloat(segments[9]);

            // info to be displayed to user when information screen is not hidden
            infoFormat = "Location:" + location +
                    "\nDate:" + date +
                    "\nTime: " + time +
                    "\nCategory: " + category +
                    "\nMagnitude: " + magnitude +
                    "\nDepth: " + depth + " km" +
                    "\nLatitude: " + coordinates[0] + ", Longitude: " + coordinates[1];

            // the string to pass to ListView and display to user
            textFormat = "Location:" + location + "\nDate:" + date;
        }
    }
}
