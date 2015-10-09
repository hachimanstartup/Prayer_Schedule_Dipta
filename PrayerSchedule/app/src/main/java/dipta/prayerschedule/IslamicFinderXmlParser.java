package dipta.prayerschedule;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    public DailyPrayer parse(InputStream in) throws XmlPullParserException, IOException {
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
