package nu.paheco.patrik.emondata;

import android.os.AsyncTask;
import android.util.Log;

class getData extends AsyncTask<String, Void, String> {

    // URL to get Emoncms JSON data
    private static String url = "http://emoncms.org/feed/list.json?apikey=8133697b1b562f52689bd680b330cb4d";

    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_TIME = "time";
    private static final String TAG_VALUE = "value";

    @Override
    protected String doInBackground(String... params) {
        String datatime="";

        String urlstring = params[0];
        //Log.i("EmonLog", "HTTP Connecting: " + urlstring);

        //URL url = new URL(urlstring);

        // Creating service handler class instance
        ServiceHandler sh = new ServiceHandler();
        // Making a request to url and getting response
        String jsonStr = sh.makeServiceCall(urlstring, ServiceHandler.GET);
        //Log.d("Response: ", "> " + jsonStr);

        //Log.w("In getData","Ok got data");
        return jsonStr;

    }

}