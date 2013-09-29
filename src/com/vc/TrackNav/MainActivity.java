package com.vc.TrackNav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.h3r3t1c.filechooser.FileChooser;

public class MainActivity extends Activity {

    final static int REQ_CODE = 1;
    GPSTrackerService gpsTrackerService;
    String selectedFile;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void callImportTrack(View view){
//        System.out.println("importDefaultTrack clicked!!!");
        Intent intent = new Intent(this, FileChooser.class);
        startActivityForResult(intent, REQ_CODE);
    }

    public void startNavigation(View view){
        gpsTrackerService = new GPSTrackerService(MainActivity.this);

        // check if GPS enabled
        if(!gpsTrackerService.canGetLocation()){
            gpsTrackerService.showSettingsAlert();
            return;
        }

//        if (selectedFile == null){
//            Toast.makeText(this, "Please, open .GPX file first!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Intent mIntent = new Intent(this, LocalService.class);
//        mIntent.putExtra(FileChooser.SELECTED_FILE, selectedFile);

        ///
        startService(new Intent(MainActivity.this,LocalService.class));
    }


    public void stopNavigation(View view){
        gpsTrackerService.stopUsingGPS();
        stopService(new Intent(MainActivity.this, LocalService.class));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("result onActivityResult");
        //Import Track
        if(requestCode == REQ_CODE){
            if (resultCode == RESULT_OK){
                selectedFile = data.getStringExtra(FileChooser.SELECTED_FILE);
                System.out.println("selectedFile: " + selectedFile);
                Toast.makeText(this, "GPX file " + selectedFile, Toast.LENGTH_SHORT).show();
            }else if(resultCode == RESULT_CANCELED){
               // Toast.makeText(this, "RESULT_CANCELED", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
