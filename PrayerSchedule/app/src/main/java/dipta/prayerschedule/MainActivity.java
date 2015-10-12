package dipta.prayerschedule;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.message);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String calcMethod = sharedPreferences.getString("calcmethod", "");
        boolean hanafi = sharedPreferences.getBoolean("hanafi", false);
        int hanafi_int = 1 + ((hanafi) ? 1 : 0);
        String url = "http://www.islamicfinder.org/prayer_service.php?country=japan&city=sendai-shi&state=00&zipcode=&latitude=38.2500&longitude=140.8667&timezone=9&" +
                "HanfiShafi=" +
                Integer.toString(hanafi_int) +
                "&pmethod=" +
                calcMethod +
                "&fajrTwilight1=10&fajrTwilight2=10&ishaTwilight=10&ishaInterval=30&dhuhrInterval=1&maghribInterval=1&dayLight=0&simpleFormat=xml";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadXmlTask().execute(url);
        } else {
            textView.setText("No network connection available.");
        }
    }

    public void myClickHandler(View view) {
        // Gets the URL from the UI's text field.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String calcMethod = sharedPreferences.getString("calcmethod", "");
        boolean hanafi = sharedPreferences.getBoolean("hanafi", false);
        int hanafi_int = 1 + ((hanafi) ? 1 : 0);
        String url = "http://www.islamicfinder.org/prayer_service.php?country=japan&city=sendai-shi&state=00&zipcode=&latitude=38.2500&longitude=140.8667&timezone=9&" +
                "HanfiShafi=" +
                Integer.toString(hanafi_int) +
                "&pmethod=" +
                calcMethod +
                "&fajrTwilight1=10&fajrTwilight2=10&ishaTwilight=10&ishaInterval=30&dhuhrInterval=1&maghribInterval=1&dayLight=0&simpleFormat=xml";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadXmlTask().execute(url);
        } else {
            textView.setText("No network connection available.");
        }
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, IslamicFinderXmlParser.DailyPrayer>{
        @Override
        protected IslamicFinderXmlParser.DailyPrayer doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return null;
            } catch (XmlPullParserException e) {
                return null;
            }

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(IslamicFinderXmlParser.DailyPrayer dailyPrayer) {
            String text = "Prayer times in " + dailyPrayer.city+", " + dailyPrayer.country;
            text += "\n" + dailyPrayer.date;
            text += "\nFajr : " + dailyPrayer.fajr;
            text += "\nSunrise : " + dailyPrayer.sunrise;
            text += "\nDhuhr : " + dailyPrayer.dhuhr;
            text += "\nAsr : " + dailyPrayer.asr;
            text += "\nMaghrib : " + dailyPrayer.maghrib;
            text += "\nIsha : " + dailyPrayer.isha;
            textView.setText(text);

        }

        private IslamicFinderXmlParser.DailyPrayer loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            IslamicFinderXmlParser.DailyPrayer dailyPrayer;
            // Instantiate the parser
            IslamicFinderXmlParser islamicFinderXmlParser = new IslamicFinderXmlParser();

            try {
                stream = downloadUrl(urlString);
                dailyPrayer = islamicFinderXmlParser.parse(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
            // Each Entry object represents a single post in the XML feed.
            // This section processes the entries list to combine each entry with HTML markup.
            // Each entry is displayed in the UI as a link that optionally includes
            // a text summary.

            return dailyPrayer;
        }
        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private InputStream downloadUrl(String myurl) throws IOException {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();
            return conn.getInputStream();
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
