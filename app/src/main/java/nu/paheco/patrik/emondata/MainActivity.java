package nu.paheco.patrik.emondata;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Todo

public class MainActivity extends ListActivity {
    //public class MainActivity extends AppCompatActivity {
    // JSON Node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_TIME = "time";
    private static final String TAG_VALUE = "value";

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> dataList;
    String datatime;

    private ListView listView = null;
    Integer noofitems = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getEmondata();

        /*
        ListView listview = getListView();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View clickView,
                                    int position, long id) {
                //String country = list[position];
                Toast.makeText(MainActivity.this,
                        String.format("%s was chosen."),
                        Toast.LENGTH_SHORT).show();
                Log.i ("Clicked:", String.valueOf(position));
            }
        });
        */
        /*
        final ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object listItem = lv.getItemAtPosition(position);
                Log.d("Pos:", listItem.toString());
            }
        });*/

        // Get all settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, ?> prefsMap = settings.getAll();
        for (Map.Entry<String, ?> entry: prefsMap.entrySet()) {
            Log.d("SharedPreferences", entry.getKey() + ":" + entry.getValue().toString());
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                menuapikeyClicked();
                return true;
            //case R.id.help:
            //    showHelp();
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void getEmondata() {
        // Find gui elements
        TextView header = (TextView) findViewById(R.id.header);
        EditText apikey=(EditText)findViewById(R.id.apikey);
        Button btnSave = (Button) findViewById(R.id.saveapikey);
        Button btnfindapikey = (Button) findViewById(R.id.emoncms);

        // Get stored api key
        String stored_apikey = getPreferences(MODE_PRIVATE).getString("apikey", "");
        boolean corrApi=true;

        Integer apiLength = stored_apikey.length();
        if (apiLength!=32) {
            // wrong length on api key
            header.setText(R.string.wrongapikey);
            corrApi=false;
        }
        else {
            // A correct key was found, hide api textbox and save-button
            apikey.setVisibility(View.GONE);
            //btnSave.setVisibility(getListView().GONE);
            btnSave.setVisibility(View.GONE);
            //btnfindapikey.setVisibility(getListView().GONE);
            btnfindapikey.setVisibility(View.GONE);
        }
        // Print to edittext
        apikey.setText(stored_apikey);

        if (corrApi==true) {
            // URL to get Emoncms JSON data
            String url = "http://emoncms.org/feed/list.json?apikey=" + stored_apikey;
            // Init list
            dataList = new ArrayList<HashMap<String, String>>();
            //ListView lv = getListView();

            String[] from = new String[] {"name", "rtime", "id"};
            int[] lines = new int[] { R.id.name, R.id.rtime, R.id.id };

            String result="";
            try {
                result = new getData().execute(url).get();
                //result = new getData().execute("http://emoncms.org/feed/list.json?apikey=" + apikey).get();
            } catch (Exception e) {
                //Log.i("EmonLog", "Error in getData");
            }
            //Log.i("EmonLog", "Result from getData: " + result);

        //Log.i("Emonlog", "Parse result from server");
        if (result != null) {
            try {
                JSONArray jsonArr = new JSONArray(result);
                // Settings manager
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

                // looping through All items
                for (int i = 0; i < jsonArr.length(); i++) {
                    // Get values
                    JSONObject c = jsonArr.getJSONObject(i);
                    String id = c.getString(TAG_ID);
                    String name = c.getString(TAG_NAME);
                    String value = c.getString(TAG_VALUE);
                    String time = c.getString(TAG_TIME);

                    //Log.d("Id: ", id);
                    // Is there a settings for this id?
                    if (settings.contains(id)) {
                        Log.d("Id setting found: ", id);
                        String checked = settings.getString(id, "");
                        Log.d("Value ", checked);

                    }

                    HashMap<String, String> item = new HashMap<String, String>();
                    // Convert timestamp
                    long tlong = Long.parseLong(time);
                    String realtime = getDate(tlong);
                    //Log.i("Timelong: ",realtime);
                    datatime = realtime;

                    // How old are the values
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    try {
                        Date mDate = sdf.parse(realtime);
                        long timeInMilliseconds = mDate.getTime();
                        long timenow = System.currentTimeMillis();
                        long timediff = timenow - timeInMilliseconds;

                        long days = TimeUnit.MILLISECONDS.toDays(timediff);
                        timediff -= TimeUnit.DAYS.toMillis(days);
                        long hours = TimeUnit.MILLISECONDS.toHours(timediff);
                        timediff -= TimeUnit.HOURS.toMillis(hours);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(timediff);
                        timediff -= TimeUnit.MINUTES.toMillis(minutes);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(timediff);

                        //Log.i("Timelong: ", realtime);
                        //Log.i("Age: ", days + ":" + hours + ":" + minutes);

                        // Add to list
                        item.put(TAG_NAME, name + ": " + value);
                        item.put("rtime", "Retrieved @ " + realtime + "(" + days + " days, " + hours + " hours, " + minutes + " minutes ago)");
                        item.put(TAG_ID, id);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dataList.add(item);

                    // Update header with current time
                    Calendar cal = Calendar.getInstance();
                    String date = "" + cal.get(Calendar.DATE) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR);
                    String timenow = "" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
                    header.setText("Emoncms data, retreived @ " + date + " " + timenow);


                    //   SimpleAdapter k=new SimpleAdapter(this,val1,R.layout.mytask,new String[]{"a","c","b"},
                    // new int[]{R.id.View1,R.id.View2,R.id.ViewStatus})

                    //ListView lv= (ListView)findViewById(R.id.list);
                    ListAdapter adapter = new SimpleAdapter(
                            MainActivity.this, dataList,
                            R.layout.list_item, from, lines);
                    //listView.setAdapter(adapter);
                    setListAdapter(adapter);
                    noofitems = adapter.getCount();


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("Number of listitems: ", String.valueOf(noofitems));

        }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.mainmenu, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    private String getDate(long time) {
        String stime = Long.toString(time);
        //Log.i("stime: ", stime);
        Date date = new Date(time*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
        String formattedDate = sdf.format(date);
        //System.out.println(formattedDate);
        return formattedDate.toString();
    }
    public String itemClicked() {
        return "1";
    }
    public void SaveClicked(View view) {
        for (int i = 0; i < noofitems; i++) {
            TextView header = (TextView) findViewById(R.id.header);
            view.findViewById(R.id.id);


        }

        }
    public void apikeyClicked(View view){
        //if(view.getId()==R.id.apikey);
        TextView apikey = (TextView) findViewById(R.id.apikey );
        apikey.setText("");
    }
    public void saveapikeyClicked(View view){
        // Find gui elements
        TextView header = (TextView) findViewById(R.id.header);
        EditText apikey=(EditText)findViewById(R.id.apikey);
        Button btnSave = (Button) findViewById(R.id.saveapikey);
        Button btnEmoncms = (Button) findViewById(R.id.emoncms);

        // Save value
        getPreferences(MODE_PRIVATE).edit().putString("apikey",apikey.getText().toString()).commit();

        // Hide gui elements
        apikey.setVisibility(View.GONE);
        btnSave.setVisibility(getListView().GONE);
        header.setText("Api key saved");
        btnEmoncms.setVisibility(getListView().GONE);

        // Get data
        getEmondata();
    }
    public void checkClicked(View view){
        TextView textid = (TextView) findViewById(R.id.id);
        String id = textid.getText().toString();

        //code to check if this checkbox is checked!
        CheckBox checkBox = (CheckBox)view;
        String strChecked;
        if(checkBox.isChecked()){
            Log.i("Checked?",  "Yes!");
            strChecked = "yes";
        }
        else {
            strChecked = "no";
        }

        Log.i("Check clicked", "click");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(id, strChecked);
        editor.commit();

    }

    public void editapikeyClicked(View view){
        // Find gui elements
        TextView header = (TextView) findViewById(R.id.header);
        EditText apikey=(EditText)findViewById(R.id.apikey);
        Button btnSave = (Button) findViewById(R.id.saveapikey);
        // Show elements
        apikey.setVisibility(View.VISIBLE);
        btnSave.setVisibility(getListView().VISIBLE);
        header.setText(R.string.editapikey);
    }
    public void menuapikeyClicked(){
        // Find gui elements
        TextView header = (TextView) findViewById(R.id.header);
        EditText apikey=(EditText)findViewById(R.id.apikey);
        Button btnSave = (Button) findViewById(R.id.saveapikey);
        // Show elements
        apikey.setVisibility(View.VISIBLE);
        btnSave.setVisibility(getListView().VISIBLE);
        header.setText(R.string.editapikey);
    }
    public void emoncmsClicked(View view){
        String emonurl = "http://emoncms.org/user/login";
        //Log.i("Open url", emonurl);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(emonurl));
        startActivity(i);
    }
}
