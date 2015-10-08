package dipta.prayerschedule;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.message);
        TextView textView1 = new TextView(this);
    }

    public void myClickHandler(View view) {
        // Gets the URL from the UI's text field.
        String url = "http://www.islamicfinder.org/prayer_service.php?country=japan&city=sendai-shi&state=00&zipcode=&latitude=38.2500&longitude=140.8667&timezone=9&HanfiShafi=1&pmethod=1&fajrTwilight1=10&fajrTwilight2=10&ishaTwilight=10&ishaInterval=30&dhuhrInterval=1&maghribInterval=1&dayLight=0&simpleFormat=xml";
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

    public class IslamicFinderXmlParser{
        private final String ns = null;

        public class Date{
            public final String fajr;
            public final String sunrise;
            public final String dhuhr;
            public final String asr;
            public final String maghrib;
            public final String isha;
            public final String day;
            public final String month;
            public final String year;
            public final String week_day;

            private Date(String fajr,String sunrise, String dhuhr, String asr, String maghrib, String isha, String day, String month, String year, String week_day){
                this.fajr = fajr;
                this.sunrise = sunrise;
                this.dhuhr = dhuhr;
                this.asr = asr;
                this.maghrib = maghrib;
                this.isha = isha;
                this.day = day;
                this.month = month;
                this.year = year;
                this.week_day = week_day;
            }
        }

        public class DailyPrayer{
            public final String fajr;
            public final String sunrise;
            public final String dhuhr;
            public final String asr;
            public final String maghrib;
            public final String isha;
            public final String date;
            public final String hijri;
            public final String city;
            public final String country;

            private DailyPrayer(String fajr,String sunrise, String dhuhr, String asr, String maghrib, String isha, String date, String hijri, String city, String country){
                this.fajr = fajr;
                this.sunrise = sunrise;
                this.dhuhr = dhuhr;
                this.asr = asr;
                this.maghrib = maghrib;
                this.isha = isha;
                this.date = date;
                this.hijri = hijri;
                this.city = city;
                this.country = country;
            }
        }

        public DailyPrayer parse(InputStream in) throws XmlPullParserException, IOException{
            try{
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readDaily(parser);
                //return readFeed(parser);
            }finally {
                in.close();
            }
        }

        private DailyPrayer readDaily(XmlPullParser parser) throws XmlPullParserException, IOException {
            String fajr = null;
            String sunrise = null;
            String dhuhr = null;
            String asr = null;
            String maghrib = null;
            String isha = null;
            String date = null;
            String hijri = null;
            String city = null;
            String country = null;
            parser.require(XmlPullParser.START_TAG,ns,"prayer");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("fajr")) {
                    parser.require(XmlPullParser.START_TAG, ns, "fajr");
                    fajr = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "fajr");
                } else if (name.equals("sunrise")) {
                    parser.require(XmlPullParser.START_TAG, ns, "sunrise");
                    sunrise = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "sunrise");
                } else if (name.equals("dhuhr")) {
                    parser.require(XmlPullParser.START_TAG, ns, "dhuhr");
                    dhuhr = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "dhuhr");
                } else if (name.equals("asr")) {
                    parser.require(XmlPullParser.START_TAG, ns, "asr");
                    asr = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "asr");
                } else if (name.equals("maghrib")) {
                    parser.require(XmlPullParser.START_TAG, ns, "maghrib");
                    maghrib = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "maghrib");
                } else if (name.equals("isha")) {
                    parser.require(XmlPullParser.START_TAG, ns, "isha");
                    isha = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "isha");
                } else if (name.equals("date")) {
                    parser.require(XmlPullParser.START_TAG, ns, "date");
                    date = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "date");
                }else if (name.equals("hijri")) {
                    parser.require(XmlPullParser.START_TAG, ns, "hijri");
                    hijri = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "hijri");
                }else if (name.equals("city")) {
                    parser.require(XmlPullParser.START_TAG, ns, "city");
                    city = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "city");
                }else if (name.equals("country")) {
                    parser.require(XmlPullParser.START_TAG, ns, "country");
                    country = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "country");
                }else {
                    skip(parser);
                }
            }
            parser.require(XmlPullParser.END_TAG, ns, "prayer");
            return new DailyPrayer(fajr,sunrise,dhuhr,asr,maghrib,isha,date,hijri,city,country);
        }

        private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{
            List entries = new ArrayList();

            parser.require(XmlPullParser.START_TAG,ns,"prayer");
            while(parser.next() != XmlPullParser.END_TAG){
                if(parser.getEventType() != XmlPullParser.START_TAG){
                    continue;
                }
                String name = parser.getName();
                if(name.equals("date")) {
                    entries.add(readDate(parser));
                }else{
                    skip(parser);
                }
            }
            return entries;
        }

        private Date readDate(XmlPullParser parser) throws XmlPullParserException, IOException{
            String fajr = null;
            String sunrise = null;
            String dhuhr = null;
            String asr = null;
            String maghrib = null;
            String isha = null;
            String day = null;
            String month = null;
            String year = null;
            String week_day = null;
            parser.require(XmlPullParser.START_TAG, ns, "date");
            day = parser.getAttributeValue(null,"day");
            month = parser.getAttributeValue(null,"month");
            year = parser.getAttributeValue(null,"year");
            week_day = parser.getAttributeValue(null,"week_day");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("fajr")) {
                    parser.require(XmlPullParser.START_TAG, ns, "fajr");
                    fajr = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "fajr");
                } else if (name.equals("sunrise")) {
                    parser.require(XmlPullParser.START_TAG, ns, "sunrise");
                    sunrise = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "sunrise");
                } else if (name.equals("dhuhr")) {
                    parser.require(XmlPullParser.START_TAG, ns, "dhuhr");
                    dhuhr = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "dhuhr");
                } else if (name.equals("asr")) {
                    parser.require(XmlPullParser.START_TAG, ns, "asr");
                    asr = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "asr");
                } else if (name.equals("maghrib")) {
                    parser.require(XmlPullParser.START_TAG, ns, "maghrib");
                    maghrib = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "maghrib");
                } else if (name.equals("isha")) {
                    parser.require(XmlPullParser.START_TAG, ns, "isha");
                    isha = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "isha");
                } else {
                    skip(parser);
                }
            }
            parser.require(XmlPullParser.END_TAG, ns, "date");
            return new Date(fajr,sunrise,dhuhr,asr,maghrib,isha,day,month,year,week_day);
        }

        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
