
// Name                 Iain Cotter
// Student ID           S1507974
// Programme of Study   Computer Games (Software Development)
//


package me.gcu.cotter_s1507974;

import android.os.Looper;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class Parser {

    private String itemTag = "item";
    private String titleTag = "title";
    private String descTag = "description";
    private String linkTag = "link";
    private String dateTag = "pubDate";
    private String catTag = "category";
    private String latTag = "lat";
    private String longTag = "long";

    me.gcu.cotter_s1507974.MainActivity main;

    XmlPullParserFactory xfo = null;
    XmlPullParser p = null;

    public Parser(me.gcu.cotter_s1507974.MainActivity mainActivity){
        main = mainActivity;
    }

    public void parseXmlString(String sData, MainActivity activity) throws XmlPullParserException, IOException{

        main = activity;
        try {
            xfo = XmlPullParserFactory.newInstance();
            xfo.setNamespaceAware(true);
            p = xfo.newPullParser();

            sData = sData.substring(4);
            Log.e("Timer", sData);
            p.setInput(new StringReader(sData));
            int evt = p.getEventType();

            String tag = "";
            boolean read = false;
            String output = "";

            ArrayList<String> outputs = new ArrayList<String>();

            while (evt != XmlPullParser.END_DOCUMENT) {
                switch (evt) {
                    case XmlPullParser.START_TAG:
                        tag = p.getName();
                        if(itemTag.equals(tag)){
                            read = true;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tag = p.getName();
                        if(itemTag.equals(tag)){
                            read = false;
                            outputs.add(output);
                            output = "";
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if(read == true) {
                            if (descTag.equals(tag) || dateTag.equals(tag) || catTag.equals(tag) || latTag.equals(tag) || longTag.equals(tag)) {
                                output += " ; " + p.getText();
                            }
                        }
                        break;
                }
                evt = p.next();
            }

            Log.e("Timer", "Reading");
            main.AddToList(outputs);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
