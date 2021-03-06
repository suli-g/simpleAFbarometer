package com.absalasa.xyz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    String[] data;
    GPSData XYZ;
    Location location;
    double pressure;
    StringBuilder FILE = new StringBuilder();
    FileOutputStream Out;
    Context ctx;
    File fileLocation;
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
    Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();
        //Variables
        StateView = findViewById(R.id.textView);
        StartStopBtn = findViewById(R.id.button);
        res = getResources();
        STOP = getString(R.string.stop);
        START = getString(R.string.start);
        RECORDING = getString(R.string.recording);
        HEADINGS = getString(R.string.headings);
        INTENT = getString(R.string.intent);
        AUTHORITY = getString(R.string.authority);
        SAVED_TO = getString(R.string.saved_to, getFilesDir(), CSV_FILE);
        CSV_FILE = getString(R.string.csvFile);
        HEADER_TYPE = getString(R.string.headerType);
        ENABLED_GPS = getString(R.string.enable_gps);
        PERMISSION_DENIED = getString(R.string.permission_denied);
        StartStopBtn.setOnClickListener(handleClick);
        data = new String[4];
    }

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
                location = XYZ.getLocation();
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
                    Toast.makeText(MainActivity.this, SAVED_TO, Toast.LENGTH_LONG).show();
                    Out.close();
                    exportFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//onclick
    };

    public void exportFile() {
        //exporting file
        fileLocation = new File(getFilesDir(), CSV_FILE);
        path =  FileProvider.getUriForFile(ctx, AUTHORITY, fileLocation);
        fileIntent = new Intent(Intent.ACTION_SEND);
        fileIntent.setType(HEADER_TYPE);
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
        startActivity(Intent.createChooser(fileIntent, INTENT));
    }

   //runs loop until stopped by StartStopBtn
    public class GPSDataLoop extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

       @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (location != null) {
                    while (working) {
                        location = XYZ.getLocation();
                        pressure = XYZ.getPressure();
                        //get coordinates
                        data[0] = String.valueOf(location.getLatitude());
                        data[1] = String.valueOf(location.getLongitude());
                        data[2] = String.valueOf(location.getAltitude());
                        data[3] = String.valueOf(pressure);
                        //append file
                        FILE.append(getString(R.string.coordinates, data[0], data[1] , data[2], data[3]));
                        pause(res.getInteger(R.integer.defaultPause));
                        //XYZ = new GPSdata(getApplicationContext());
                        //Location = XYZ.getLocation();
                        //GPS();
                        if (!working) break;
                    }//while loop
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }//background
    }//GPS data loop


    public class GPSData implements LocationListener, SensorEventListener {
        Context context;
        SensorManager sensorManager;
        LocationManager locationManager;
        Sensor pressureSensor;

        GPSData(Context c) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sensorManager != null){
                pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_UI);
            }
            context = c;
        }
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] values = sensorEvent.values;
                pressure = values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        double getPressure() {
            return pressure;
        }

        Location getLocation() {
            boolean permission_granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (permission_granted) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && sensorManager != null){
                boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (gps_enabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,this);
                    Location L = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    return L;
                }
            }
            String ERROR = permission_granted ? PERMISSION_DENIED : ENABLED_GPS;
            Toast.makeText(context,ERROR,Toast.LENGTH_SHORT).show();
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

        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
