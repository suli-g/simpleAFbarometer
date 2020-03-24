package com.absalasa.xyz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //variables
    boolean State = false;//Checks StartStopBtn state
    int i = 0;
    TextView StateView;
    Button  StartStopBtn;
    Double XCoord;
    Double YCoord;
    Double ZCoord;
    GPSData XYZ;
    Location Location;
    StringBuilder FILE = new StringBuilder();
    FileOutputStream Out;
    Context contx;
    File filelocation;
    Uri path;
    Intent fileIntent;

    // Strings
    String STOP;
    String START;
    String RECORDING;
    String HEADINGS;
    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Variables
        StateView = findViewById(R.id.textView);
        StartStopBtn = findViewById(R.id.button);

        STOP = getString(R.string.stop);
        START = getString(R.string.start);
        RECORDING = getString(R.string.recording);
        HEADINGS = getString(R.string.headings);
        //click listeners
        StartStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (!State){
                StartStopBtn.setText(STOP);
                State=true;
                StateView.setText(RECORDING);
                i++;
                XYZ = new GPSData(getApplicationContext());
                Location = XYZ.getLocation();
                FILE = new StringBuilder();
                FILE.append(HEADINGS);
                GPSDataLoop LOOP = new GPSDataLoop();
                LOOP.execute();
            }else {
                StartStopBtn.setText(START);
                State=false;
                StateView.setText(getString(R.string.saved_to, getFilesDir()));
                i++;
                if (i > 0 && !State) {//make sure button was pressed more than once

                    //Out = null;
                    try {
                        Out = openFileOutput("XYZ.csv", Context.MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        //saving file
                        Out.write(FILE.toString().getBytes());
                        //Toast.makeText(MainActivity.this, "Saved to: " + getFilesDir() + "/" + "XYZ.csv", Toast.LENGTH_LONG).show();
                        Out.close();

                        //exporting file
                        contx = getApplicationContext();
                        filelocation = new File(getFilesDir(), getString(R.string.csvFile));
                        path =  FileProvider.getUriForFile(contx, getString(R.string.authority), filelocation);
                        fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType(getString(R.string.headerType));
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(fileIntent, getString(R.string.intent)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            }//onclick
        });//listener
    }//OnCreate

    public void GPS (){
        XYZ = new GPSData(getApplicationContext());
        Location = XYZ.getLocation();
            }

    //runs loop until stopped by StartStopBtn
    public class GPSDataLoop extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //GPS();
        }
        protected void onPostExecute() {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (Location != null) {
                    while (State) {//State

                        Location = XYZ.getLocation();
                        //get coordinates
                        XCoord = Location.getLatitude();
                        YCoord = Location.getLongitude();
                        ZCoord = Location.getAltitude();
                        //append file
                        FILE.append(getString(R.string.coordinates,XCoord, YCoord, ZCoord));
                        //XCoord = null;
                        //YCoord = null;
                        //ZCoord = null;
                        i++;
                        pause(2000);
                        //XYZ = new GPSdata(getApplicationContext());
                        //Location = XYZ.getLocation();
                        //GPS();
                        if (!State) break;
                    }//while loop
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }//background
    }//GPS data loop


    public class GPSData implements LocationListener {
        Context context;


        public GPSData(Context c) {
            context = c;
        }

        public Location getLocation() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context,"Permission denied",Toast.LENGTH_SHORT).show();
                return null;
            }
            LocationManager LM = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = LM.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled){
                LM.requestLocationUpdates(LM.GPS_PROVIDER,500,0,this);
                Location L = LM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return L;
            }else{
                Toast.makeText(context,"Enable GPS",Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


}//Mainactivity
