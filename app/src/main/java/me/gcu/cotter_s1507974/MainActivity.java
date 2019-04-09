
// Name                 Iain Cotter
// Student ID           S1507974
// Programme of Study   Computer Games (Software Development)
//

// Update the package name to include your Student Identifier
package me.gcu.cotter_s1507974;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private ListAdapter adapter;
    private ListView display;

    private LinearLayout info;
    private TextView info_text;
    private ImageButton quit_info;

    private Spinner sort_type;

    enum typesToSort {DATE, TIME, LOCATION, CATEGORY, MAGNITUDE, DEPTH, NORTH, EAST}

    typesToSort sort = typesToSort.DATE;
    private Switch asc_option;
    private boolean asc = false;

    Spinner search_by;

    enum searchType {DATE, LOCATION, DAY}

    searchType currentSearch = searchType.DATE;

    LinearLayout day_search;
    Spinner day_selection;

    LinearLayout date_search;
    Spinner date_selection;
    Spinner month_selection;
    String year[] = {"Year...", "", ""};
    Spinner year_selection;
    private EditText location_search;
    private Button search_button;
    private boolean searching = false;

    private Button update_button;

    private ArrayList<ListData> originalList = new ArrayList<ListData>();
    private ArrayList<ListData> displayList = new ArrayList<ListData>();
    private ArrayList<ListData> inclusionList = new ArrayList<ListData>();

    private Parser parseObject;
    private String result;
    private String url1 = "";
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";

    Timer timer = new Timer();

    int timeInSeconds = 10;

    public void hideKeyboard(MainActivity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void SetUIElements() {

        sort_type = (Spinner) findViewById(R.id.sort_type);
        ArrayAdapter<CharSequence> sortAdap = ArrayAdapter.createFromResource(this, R.array.sortTypes, android.R.layout.simple_spinner_item);
        sortAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_type.setAdapter(sortAdap);
        sort_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (sort_type.getSelectedItem().toString()) {
                    case "Date":
                        sort = typesToSort.DATE;
                        break;
                    case "Time":
                        sort = typesToSort.TIME;
                        break;
                    case "Location":
                        sort = typesToSort.LOCATION;
                        break;
                    case "Category":
                        sort = typesToSort.CATEGORY;
                        break;
                    case "Magnitude":
                        sort = typesToSort.MAGNITUDE;
                        break;
                    case "Depth":
                        sort = typesToSort.DEPTH;
                        break;
                    case "North":
                        sort = typesToSort.NORTH;
                        break;
                    case "East":
                        sort = typesToSort.EAST;
                        break;
                }
//                OrderList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        asc_option = (Switch) findViewById(R.id.asc_option);
        asc_option.setChecked(asc);
        asc_option.setOnClickListener(this);

        search_by = (Spinner) findViewById(R.id.search_by);
        ArrayAdapter<CharSequence> searchAdap = ArrayAdapter.createFromResource(this, R.array.searchTypes, android.R.layout.simple_spinner_item);
        searchAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        search_by.setAdapter(searchAdap);
        search_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                day_search.setVisibility(View.GONE);
                date_search.setVisibility(View.GONE);
                location_search.setVisibility(View.GONE);

                switch (search_by.getSelectedItem().toString()) {
                    case "Date":
                        date_search.setVisibility(View.VISIBLE);
                        currentSearch = searchType.DATE;
                        break;
                    case "Location":
                        location_search.setVisibility(View.VISIBLE);
                        currentSearch = searchType.LOCATION;
                        break;
                    case "Day":
                        day_search.setVisibility(View.VISIBLE);
                        currentSearch = searchType.DAY;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        date_selection = (Spinner) findViewById(R.id.date_selection);
        ArrayAdapter<CharSequence> dateAdap = ArrayAdapter.createFromResource(this, R.array.Dates, android.R.layout.simple_spinner_item);
        dateAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_selection.setAdapter(dateAdap);

        month_selection = (Spinner) findViewById(R.id.month_selection);
        ArrayAdapter<CharSequence> monthAdap = ArrayAdapter.createFromResource(this, R.array.Months, android.R.layout.simple_spinner_item);
        monthAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month_selection.setAdapter(monthAdap);

        date_search = (LinearLayout) findViewById(R.id.date_search);
        year[1] = "" + (Calendar.getInstance().get(Calendar.YEAR) - 1);
        year[2] = "" + Calendar.getInstance().get(Calendar.YEAR);
        year_selection = (Spinner) findViewById(R.id.year_selection);
        ArrayAdapter<String> yearAdap = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, year);
        yearAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year_selection.setAdapter(yearAdap);

        day_search = (LinearLayout) findViewById(R.id.day_search);
        day_selection = (Spinner) findViewById(R.id.day_selection);
        ArrayAdapter<CharSequence> dayAdap = ArrayAdapter.createFromResource(this, R.array.Days, android.R.layout.simple_spinner_item);
        dayAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day_selection.setAdapter(dayAdap);

        location_search = (EditText) findViewById(R.id.location_search);
        search_button = (Button) findViewById(R.id.search_button);
        search_button.setOnClickListener(this);


        update_button = (Button) findViewById(R.id.update_button);
        update_button.setOnClickListener(this);

        info = (LinearLayout) findViewById(R.id.info);
        info_text = (TextView) findViewById(R.id.info_text);
        quit_info = (ImageButton) findViewById(R.id.quit_info);
        quit_info.setOnClickListener(this);

        display = (ListView) findViewById(R.id.display);
        display.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (displayList.size() > 0) {
                    info.setVisibility(View.VISIBLE);
                    info_text.setText(displayList.get(position).infoFormat);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main_por);
//        } else {
//            setContentView(R.layout.activity_main_land);
//        }

        SetUIElements();

        if(Looper.getMainLooper().getThread() == Thread.currentThread()) {
            Log.e("Thread", "Main Thread" );
        }else{
            Log.e("Thread", "Not Main Thread" );
        }
//        timer.schedule(new ReadXML(urlSource), 0, timeInSeconds * 1000);
        timer.schedule(new ReadXML(urlSource), 0);
    }

    public void onClick(View aview) {
        switch (aview.getId()) {
            case R.id.search_button:
                hideKeyboard(this);
                String input = "";
                switch (currentSearch) {
                    case DATE:
                        String date = date_selection.getSelectedItem().toString();
                        date = date.matches("Date") ? "" : date_selection.getSelectedItem().toString();
                        String month = month_selection.getSelectedItem().toString();
                        month = month.matches("Month") ? "" : month_selection.getSelectedItem().toString();
                        String year = year_selection.getSelectedItem().toString() == "Year" ? "" : year_selection.getSelectedItem().toString();

                        input = date + " " + month + " " + year;
                        input = input.trim();
                        break;
                    case LOCATION:

                        input = location_search.getText().toString().trim();
                        break;
                    case DAY:
                        String day = day_selection.getSelectedItem().toString();
                        day = day.matches("Days") ? "" : day_selection.getSelectedItem().toString();

                        input = day;
                        break;
                }
                inclusionList.clear();
                if (!input.matches("")) {
                    SearchList(input);
                } else {
                    searching = false;
                }
                OrderList();
                break;
            case R.id.asc_option:
                asc = !asc;
                OrderList();
                break;
            case R.id.quit_info:
                info.setVisibility(View.GONE);
                break;
            case R.id.update_button:
                update_button.setVisibility(View.GONE);
//                timer.schedule(new TimeToUpdateTask(), 0, timeInSeconds * 1000);
                break;
        }
    }

    public void SearchList(String input) {

        searching = true;

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
                if (!inclusionList.contains(data)) {
                    inclusionList.add(data);
                }
            }
        }
    }

    public void OrderList() {

        displayList.clear();

        if (searching == false) {
            for (ListData data : originalList) {
                displayList.add(data);
            }
        } else {
            for (ListData data : originalList) {
                if (inclusionList.contains(data)) {
                    displayList.add(data);
                }
            }
        }

        switch (sort) {
            case TIME:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return o1.time.compareTo(o2.time);
                    }
                });
                break;
            case LOCATION:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return o1.location.compareTo(o2.location);
                    }
                });
                break;
            case CATEGORY:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return o1.category.compareTo(o2.category);
                    }
                });
                break;
            case MAGNITUDE:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o1.magnitude, o2.magnitude);
                    }
                });
                break;
            case DEPTH:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o1.depth, o2.depth);
                    }
                });
                break;
            case NORTH:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o2.coordinates[0], o1.coordinates[0]);
                    }
                });
                break;
            case EAST:
                Collections.sort(displayList, new Comparator<ListData>() {
                    @Override
                    public int compare(ListData o1, ListData o2) {
                        return Float.compare(o2.coordinates[1], o1.coordinates[1]);
                    }
                });
                break;
        }

        if (asc == true) {
            Collections.reverse(displayList);
        }

        ArrayList<String> temp = new ArrayList<String>();

        for (ListData displayData : displayList) {
            temp.add(displayData.textFormat);
        }

        if (searching == true) {
            if (temp.size() == 0) {
                temp.add("The Search Produced No Results...");
            }
        }

        String[] arr = temp.toArray(new String[temp.size()]);
        setDisplay(arr);
    }

    public void AddToList(ArrayList<String> list) {

        for(String listItem: list){
            ListData temp = new ListData(listItem);
            originalList.add(temp);
        }

        Log.e("Timer", "List was Updated!");

        OrderList();
    }

    public void setDisplay(String[] newDisplay) {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, newDisplay);
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                display.setAdapter(adapter);
            }
        });
    }

    public class ListData{
        String location = "";
        String date = "";
        String time = "";
        String category = "";
        float magnitude = 0f;
        float depth = 0f;
        float [] coordinates = {0f, 0f};
        String textFormat;
        String infoFormat;

        public ListData(String text){
            String[] segments = text.split(";");

            location = segments[2].substring(10, segments[2].length());
            date = segments[6].substring(0, segments[6].length() -9);
            time = segments[6].substring(segments[6].length() -9);
            category = segments[7];
            magnitude = Float.parseFloat(segments[5].substring(12, segments[5].length()));
            depth = Float.parseFloat(segments[4].substring(8, segments[4].length() -3));
            coordinates[0] = Float.parseFloat(segments[8]);
            coordinates[1] = Float.parseFloat(segments[9]);

            infoFormat = "Location:" + location +
                    "\nDate:" + date +
                    "\nTime: " + time +
                    "\nCategory: " + category +
                    "\nMagnitude: " + magnitude +
                    "\nDepth: " + depth + " km" +
                    "\nLatitude: " + coordinates[0] + ", Longitude: " + coordinates[1];

            textFormat = "Location:" + location + "\nDate:" + date;
        }
    }

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class ReadXML extends TimerTask {
        private String url;

        public ReadXML(String aurl)
        {
            url = aurl;
        }
        @Override
        public void run()
        {

            URL url;
            URLConnection urlConnection;
            BufferedReader input = null;
            String line = "";

            try
            {
                url = new URL(this.url);
                urlConnection = url.openConnection();
                input = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = input.readLine()) != null)
                {
                    result += line;
                }
                input.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            try {
                Parser parser = new Parser(MainActivity.this);
                parser.parseXmlString(result , MainActivity.this);
            }catch (XmlPullParserException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}

