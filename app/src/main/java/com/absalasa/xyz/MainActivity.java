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
    boolean working = false;
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
    String STOP;
    String START;
    String RECORDING;
    String HEADINGS;
    String INTENT;
    String AUTHORITY;
    String SAVED_TO;
    String CSV_FILE;
    String HEADER_TYPE;
    String ENABLED_GPS;
    String PERMISSION_DENIED;
    //pause loop for X seconds
    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    protected void toggleWork() {
        working = !working;
        if (working) {
            StartStopBtn.setText(STOP);
            StateView.setText(RECORDING);
        } else {
            StartStopBtn.setText(START);
            StateView.setText(SAVED_TO);
        }
    }

    View.OnClickListener handleClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleWork();
            if (working){
                XYZ = new GPSData(getApplicationContext());
                Location = XYZ.getLocation();
                FILE = new StringBuilder();FILE.append(HEADINGS);
                GPSDataLoop LOOP = new GPSDataLoop();
                LOOP.execute();
            }
            else {
                    //Out = null;
                    try {
                        Out = openFileOutput(CSV_FILE, Context.MODE_PRIVATE);
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
                        filelocation = new File(getFilesDir(), CSV_FILE);
                        path =  FileProvider.getUriForFile(contx, AUTHORITY, filelocation);
                        fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType(HEADER_TYPE);
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(fileIntent, INTENT));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }//onclick
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Variables
        StateView = (TextView) findViewById(R.id.textView);
        StartStopBtn = (Button) findViewById(R.id.button);
        STOP = getString(R.string.stop);
        START = getString(R.string.start);
        RECORDING = getString(R.string.recording);
        HEADINGS = getString(R.string.headings);
        INTENT = getString(R.string.intent);
        AUTHORITY = getString(R.string.authority);
        SAVED_TO = getString(R.string.saved_to, getFilesDir());
        CSV_FILE = getString(R.string.csvFile);
        HEADER_TYPE = getString(R.string.headerType);
        ENABLED_GPS = getString(R.string.enable_gps);
        PERMISSION_DENIED = getString(R.string.permission_denied);
        //click listeners
        StartStopBtn.setOnClickListener(handleClick);
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
                    while (working == true) {//State == true

                        Location = XYZ.getLocation();
                        //get coordinates
                        XCoord = Location.getLatitude();
                        YCoord = Location.getLongitude();
                        ZCoord = Location.getAltitude();
                        //append file
                        FILE.append("\n" + String.valueOf(XCoord) + "," + String.valueOf(YCoord) + "," + String.valueOf(ZCoord));
                        //XCoord = null;
                        //YCoord = null;
                        //ZCoord = null;
                        pause(2000);
                        //XYZ = new GPSdata(getApplicationContext());
                        //Location = XYZ.getLocation();
                        //GPS();
                        if (working == false) break;
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
            boolean permission_granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (permission_granted){
                LocationManager LM = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = LM != null && LM.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!gps_enabled) {
                    Toast.makeText(context,ENABLED_GPS,Toast.LENGTH_LONG).show();
                }
                else {
                    LM.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,this);
                    Location L = LM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    return L;
                }
            }
            else {
                Toast.makeText(context,PERMISSION_DENIED,Toast.LENGTH_SHORT).show();
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
}
