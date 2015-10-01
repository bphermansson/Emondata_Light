package nu.paheco.patrik.emondatalight;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

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
        ListView lv = getListView();

        String result="";
        try {
            result = new getData().execute("http://emoncms.org/feed/list.json?apikey=8133697b1b562f52689bd680b330cb4d").get();
        } catch (Exception e) {
            Log.i("EmonLog", "Error in getData");
        }
        Log.i("EmonLog", "Result from getData: " + result);

        Log.i ("Emonlog","Parse result from server");
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
                    Log.i("Timelong: ",realtime);
                    datatime = realtime;
                    item.put(TAG_NAME, name + ": " + value);
                    dataList.add(item);

                    // Update header
                    header.setText("Emoncms data, retrived @ " + datatime);

                    ListAdapter adapter = new SimpleAdapter(
                            MainActivity.this, dataList,
                            R.layout.list_item, new String[]{TAG_NAME}, new int[]{R.id.name});
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
}