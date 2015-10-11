package nu.paheco.patrik.emondatalight;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
import java.util.concurrent.TimeUnit;

public class MainActivity extends ListActivity {

    // URL to get Emoncms JSON data
    private static String url = "http://emoncms.org/feed/list.json?apikey=8133697b1b562f52689bd680b330cb4d";

    // JSON Node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_TIME = "time";
    private static final String TAG_VALUE = "value";

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> dataList;
    String datatime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView header = (TextView) findViewById(R.id.header);

        // Init list
        dataList = new ArrayList<HashMap<String, String>>();
        //ListView lv = getListView();

        String[] from = new String[] {"name", "rtime"};
        int[] lines = new int[] { R.id.name, R.id.rtime };

        String result="";
        try {
            result = new getData().execute("http://emoncms.org/feed/list.json?apikey=8133697b1b562f52689bd680b330cb4d").get();
        } catch (Exception e) {
            Log.i("EmonLog", "Error in getData");
        }
        Log.i("EmonLog", "Result from getData: " + result);

        Log.i("Emonlog", "Parse result from server");
        if (result != null) {
            try {
                JSONArray jsonArr = new JSONArray(result);

                // looping through All items
                for (int i = 0; i < jsonArr.length(); i++) {

                    JSONObject c = jsonArr.getJSONObject(i);
                    String id = c.getString(TAG_ID);
                    String name = c.getString(TAG_NAME);
                    String value = c.getString(TAG_VALUE);
                    String time = c.getString(TAG_TIME);

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
                        //System.out.println("Date in milli :: " + timeInMilliseconds);
                        long timenow= System.currentTimeMillis();
                        long timediff = timenow - timeInMilliseconds;

                        long days = TimeUnit.MILLISECONDS.toDays(timediff);
                        timediff -= TimeUnit.DAYS.toMillis(days);
                        long hours = TimeUnit.MILLISECONDS.toHours(timediff);
                        timediff -= TimeUnit.HOURS.toMillis(hours);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(timediff);
                        timediff -= TimeUnit.MINUTES.toMillis(minutes);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(timediff);

                        Log.i("Timelong: ",realtime);
                        Log.i ("Age: ", days + ":" + hours + ":" + minutes);

                        // Add to list
                        item.put(TAG_NAME, name + ": " + value);
                        item.put("rtime", "Retrieved @ " + realtime + "(" + days + " days, " + hours + " hours," + minutes + " minutes ago)");

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dataList.add(item);

                    // Update header with current time
                    Calendar cal = Calendar.getInstance();
                    String date = ""+cal.get(Calendar.DATE)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.YEAR);
                    String timenow = ""+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);
                    header.setText("Emoncms data, retreived @ " + date + " " + timenow);

                    //ListAdapter adapter = new SimpleAdapter(
                    //        MainActivity.this, dataList,
                    //        R.layout.list_item, new String[]{TAG_NAME}, new int[]{R.id.name});
                    ListAdapter adapter = new SimpleAdapter(
                            MainActivity.this, dataList,
                            R.layout.list_item, from, lines);
                    setListAdapter(adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("yyyy-MM-dd HH:mm", cal).toString();
        return date;
    }

    public void apikeyClicked(View view){
        //if(view.getId()==R.id.apikey);
        TextView apikey = (TextView) findViewById(R.id.apikey );
        apikey.setText("");
    }
    public void saveapikeyClicked(View view){
        TextView header = (TextView) findViewById(R.id.header);
        header.setText("Not working yet");

    }
}
