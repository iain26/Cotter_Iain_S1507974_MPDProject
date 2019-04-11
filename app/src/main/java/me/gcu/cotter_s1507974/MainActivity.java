
// Name                 Iain Cotter
// Student ID           S1507974
// Programme of Study   Computer Games (Software Development)
//

package me.gcu.cotter_s1507974;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements OnClickListener, Task.Callbacks {

    // UI Elements
    private LinearLayout day_search;
    private LinearLayout date_search;
    private LinearLayout info;
    private Spinner sort_type;
    private Spinner day_selection;
    private Spinner date_selection;
    private Spinner month_selection;
    private Spinner year_selection;
    private Spinner search_by;
    private Switch asc_option;
    private EditText location_search;
    private TextView info_text;
    private ListAdapter adapter;
    private ListView display;
    private ArrayAdapter<CharSequence> spinnerAdapter;
    private String year[] = {"Year...", "", ""};
    private Button search_button;
    private Button clear_button;
    private ImageButton quit_info;

    // fragment to maintain list when orientation changes
    private Task task;
    private String taskTag = "task_tag";

    // call from task class
    @Override
    public void onProgressUpdate(String[] update) {
        setDisplay(update);
    }

    // soft keyboard pops up when text edited rehide it
    public void hideKeyboard() {
        MainActivity activity = this;
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    // set UI elements from xml file
    public void SetUIElements() {
        // sets the xml components to activity components
        sort_type = (Spinner) findViewById(R.id.sort_type);
        asc_option = (Switch) findViewById(R.id.asc_option);

        search_by = (Spinner) findViewById(R.id.search_by);
        clear_button = (Button) findViewById(R.id.clear_button);

        date_search = (LinearLayout) findViewById(R.id.date_search);
        date_selection = (Spinner) findViewById(R.id.date_selection);
        month_selection = (Spinner) findViewById(R.id.month_selection);
        year_selection = (Spinner) findViewById(R.id.year_selection);

        day_search = (LinearLayout) findViewById(R.id.day_search);
        day_selection = (Spinner) findViewById(R.id.day_selection);

        location_search = (EditText) findViewById(R.id.location_search);
        search_button = (Button) findViewById(R.id.search_button);

        info = (LinearLayout) findViewById(R.id.info);
        info_text = (TextView) findViewById(R.id.info_text);
        quit_info = (ImageButton) findViewById(R.id.quit_info);

        display = (ListView) findViewById(R.id.display);

        // set event listeners on activity components
        search_button.setOnClickListener(this);
        clear_button.setOnClickListener(this);
        quit_info.setOnClickListener(this);
        asc_option.setOnClickListener(this);

        // set switch to users previous setting
        asc_option.setChecked(task.asc);

        // spinner string values are loaded from the strings.xml in value folder
        // Set spinner and events for sort that update tasks sort type and updates list view component
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sortTypes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_type.setAdapter(spinnerAdapter);
        sort_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
                switch (sort_type.getSelectedItem().toString()) {
                    case "Date":
                        task.sort = Task.typesToSort.DATE;
                        break;
                    case "Time":
                        task.sort = Task.typesToSort.TIME;
                        break;
                    case "Location":
                        task.sort = Task.typesToSort.LOCATION;
                        break;
                    case "Category":
                        task.sort = Task.typesToSort.CATEGORY;
                        break;
                    case "Magnitude":
                        task.sort = Task.typesToSort.MAGNITUDE;
                        break;
                    case "Depth":
                        task.sort = Task.typesToSort.DEPTH;
                        break;
                    case "Northern":
                        task.sort = Task.typesToSort.NORTHERN;
                        break;
                    case "Eastern":
                        task.sort = Task.typesToSort.EASTERN;
                        break;
                }
                task.OrderList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set spinner and events for search that update tasks search type
        // if type changed clear previous search update list view component
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.searchTypes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        search_by.setAdapter(spinnerAdapter);
        search_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();

                day_search.setVisibility(View.GONE);
                date_search.setVisibility(View.GONE);
                location_search.setVisibility(View.GONE);

                switch (search_by.getSelectedItem().toString()) {
                    case "Date":
                        date_search.setVisibility(View.VISIBLE);
                        task.currentSearch = Task.searchType.DATE;
                        break;
                    case "Location":
                        location_search.setVisibility(View.VISIBLE);
                        task.currentSearch = Task.searchType.LOCATION;
                        break;
                    case "Day":
                        day_search.setVisibility(View.VISIBLE);
                        task.currentSearch = Task.searchType.DAY;
                        break;
                }

                if(task.lastSearch != task.currentSearch){
                    task.lastSearch = task.currentSearch;
                    task.inclusionList.clear();
                    task.searching = false;

                    task.OrderList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // sets date spinners for date, month and year
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.Dates, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_selection.setAdapter(spinnerAdapter);
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.Months, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month_selection.setAdapter(spinnerAdapter);
        year[1] = "" + (Calendar.getInstance().get(Calendar.YEAR) - 1);
        year[2] = "" + Calendar.getInstance().get(Calendar.YEAR);
        ArrayAdapter<String> yearAdap = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, year);
        yearAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year_selection.setAdapter(yearAdap);

        // sets spinner for day
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.Days, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day_selection.setAdapter(spinnerAdapter);

        // sets event when iten in list selected
        // only able to select if the list to display exists
        display.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
                if (task.displayList.size() > 0) {
                    // vibrates on item select
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    else{
                        vibrator.vibrate(50);
                    }

                    // display additional information based on item selected
                    info.setVisibility(View.VISIBLE);
                    info_text.setText(task.displayList.get(position).infoFormat);
                }
            }
        });
    }

    // when activity is created on create is called
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // change to orientation will destroy current activity
        // depending on the orientation currently determine which xml layout to load
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main_por);
        } else {
            setContentView(R.layout.activity_main_land);
        }

        // fragment that runs throughout seperate instances of the activity
        // on first loadup create the fragment
        FragmentManager manager = getSupportFragmentManager();
        task = (Task)manager.findFragmentByTag(taskTag);
        if(task == null){
            task = new Task();
            manager.beginTransaction().add(task, taskTag).commit();
        }

        // set activity components to xml components
        SetUIElements();
    }

    // when buttons or switch is pressed
    public void onClick(View aview) {
        hideKeyboard();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else{
            vibrator.vibrate(50);
        }

        //check which button was pressed
        switch (aview.getId()) {
            // search was pressed
            case R.id.search_button:
                String searchInput = "";
                // depending on search condition in fragment then read the values of spinners or edit text to get string
                switch (task.currentSearch) {
                    case DATE:
                        String date = date_selection.getSelectedItem().toString();
                        date = date.contains("Date") ? "" : date_selection.getSelectedItem().toString();

                        String month = month_selection.getSelectedItem().toString();
                        month = month.contains("Month") ? "" : month_selection.getSelectedItem().toString();

                        String year = year_selection.getSelectedItem().toString().contains("Year")  ? "" : year_selection.getSelectedItem().toString();

                        searchInput = date + " " + month + " " + year;
                        searchInput = searchInput.trim();
                        break;
                    case LOCATION:
                        searchInput = location_search.getText().toString().trim();
                        break;
                    case DAY:
                        String day = day_selection.getSelectedItem().toString();
                        day = day.contains("Days") ? "" : day_selection.getSelectedItem().toString();

                        searchInput = day;
                        break;
                }
                // reset search list and check that input is not nothing then run search function in fragment
                task.inclusionList.clear();
                if (!searchInput.matches("")) {
                    task.SearchList(searchInput);
                } else {
                    task.searching = false;
                }
                task.OrderList();
                break;
            // switch changes boolean that changes the order of list
            case R.id.asc_option:
                task.asc = !task.asc;
                task.OrderList();
                break;
            // when cross pressed on information screen hide the information screen
            case R.id.quit_info:
                info.setVisibility(View.GONE);
                break;
            // resets all spinner and edit text for search and clears search list
            case R.id.clear_button:
                date_selection.setSelection(0);
                month_selection.setSelection(0);
                year_selection.setSelection(0);
                location_search.setText("");
                day_selection.setSelection(0);
                task.inclusionList.clear();
                task.searching = false;
                task.OrderList();
                break;
        }
    }

    // Updates the List View component
    public void setDisplay(String[] newDisplay) {
        //checks if data has been implemented
        if(task.searching == false && task.displayList.size() == 0){
            String[] noData = {"No Data, Check Internet Connection..."};
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, noData);
        }else {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, newDisplay);
        }
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                display.setAdapter(adapter);
            }
        });
    }
}

