package com.vc.TrackNav;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import com.h3r3t1c.filechooser.FileChooser;
import net.divbyzero.gpx.*;
import net.divbyzero.gpx.parser.JDOM;
import net.divbyzero.gpx.parser.Parser;
import net.divbyzero.gpx.parser.ParsingException;

import java.io.File;
import java.util.*;

public class NavigationService extends Service implements TextToSpeech.OnInitListener
{
    private static Timer timer = new Timer();
    private Context ctx;

    GPX gpx = null;

    //tts
    private String str;
    private TextToSpeech mTts;
    private static final String TAG="TTSService";
    private GPSTrackerService gps;

    private ArrayList<Waypoint> waypoints;
    private double previoudDistance = 0;

    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        ctx = this;
        mTts = new TextToSpeech(this,this);
        mTts.setSpeechRate(0.3f);
        str = "hello";
        gps = new GPSTrackerService(this);
        startService();
    }

    private void startService()
    {
        timer.scheduleAtFixedRate(new mainTask(), 0, 10000);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        say(str);

        intent.getExtras();

        //import gpx file data
        String selectedFile = "/dev/test.gpx";//intent.getStringExtra(FileChooser.SELECTED_FILE);
        System.out.println("selectedFile: " + selectedFile);

        Parser parser = new JDOM();
        gpx = new GPX();
        try {
//            System.out.println("start parsing gpx");
            gpx = parser.parse(new File(selectedFile));
        } catch (ParsingException e) {
//            System.out.println("exception occured during parsing!"+e.getMessage());
            Toast.makeText(this, "GPX file error", Toast.LENGTH_SHORT).show();
            return;
        } catch (Exception e){
            Toast.makeText(this, "GPX file error!!! " + selectedFile, Toast.LENGTH_SHORT).show();
            return;
        }
       Toast.makeText(this, "GPX file contains " + gpx.getTracks().size() + " tracks", Toast.LENGTH_SHORT).show();

        //get waypoints array from track
        Track track = gpx.getTracks().get(0);
        TrackSegment ts = track.getSegments().get(0);
        waypoints = ts.getWaypoints();
        //sort waypoints by time
        Collections.sort(waypoints, new Comparator<Waypoint>() {
            public int compare(Waypoint o1, Waypoint o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });

        say(" way points have been loaded");
        super.onStart(intent, startId);
    }


    @Override
    public void onInit(int status) {
        Log.v(TAG, "oninit");
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.v(TAG, "Language is not available.");
            } else {
                say(str);
            }
        } else {
            Log.v(TAG, "Could not initialize TextToSpeech.");
        }
    }


    private class mainTask extends TimerTask
    {
        public void run()
        {
            System.out.println("Local Service cycle");
            if(gps.canGetLocation()){
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();

                double currentDistance = 1;//getDistanceToClosesrWayPoint(waypoints, latitude, longitude);

                if (currentDistance > previoudDistance){
                    say("cold cold");
                } else {
                    say("hot hot");
                }

                previoudDistance = currentDistance;
//                toastHandler.sendEmptyMessage(0);
//                say("Your Location is - \nLat: " + latitude + "\nLong: " + longitude);
            }else {
                say("Can't get GPS coordinates");
            }
        }
    }

    public void onDestroy()
    {
        say("bye");
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        timer.cancel();
        gps.stopUsingGPS();
        super.onDestroy();
    }

    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_SHORT).show();
        }
    };

    public void say(String str) {
        mTts.speak(str,
                TextToSpeech.QUEUE_FLUSH,
                null);
    }

    //Navigation helper methods

    private double getDistanceToClosesrWayPoint(ArrayList<Waypoint> waypoints, double latitude, double longitude){
        double minDistance = 999999999;
        for ( Waypoint waypoint : waypoints){
            if (waypoint != null){
                Coordinate wpCoord = waypoint.getCoordinate();
                if (wpCoord != null){
                    double wpLat = wpCoord.getLatitude();
                    double wpLong = wpCoord.getLongitude();
                    double currDistance = Math.sqrt(Math.pow(latitude - wpLat, 2) + Math.pow(longitude - wpLong, 2));
                    if (currDistance < minDistance){
                        minDistance = currDistance;
                    }
                }
            }
        }
        return minDistance;
    }



}