package com.n37bl4d3.locationtracker.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.n37bl4d3.locationtracker.Configuration;
import com.n37bl4d3.locationtracker.R;
import com.n37bl4d3.locationtracker.helpers.LogHelper;
import com.n37bl4d3.locationtracker.services.LocationService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogHelper.debugLog("\"" + name.getClassName() + "\" onServiceConnected");

            LocationService.LocalBinder localBinder = (LocationService.LocalBinder) service;

            LocationService locationService = localBinder.getService();

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.debugLog("\"" + name.getClassName() + "\" onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.verboseLog("\"" + this.getClass().getName() + "\" onCreate");

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView textView = (TextView) findViewById(R.id.content_main_text_view) ;

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String provider = intent.getStringExtra(LocationService.EXTRA_PROVIDER);
                        double latitude = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0);
                        double longitude = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0);
                        long time = intent.getLongExtra(LocationService.EXTRA_TIME, 0);
                        long elapsedRealtimeNanos = intent.getLongExtra(LocationService.EXTRA_ELAPSED_REALTIME_NANOS, 0);
                        Bundle extras = intent.getBundleExtra(LocationService.EXTRA_EXTRAS);
                        float accuracy = intent.getFloatExtra(LocationService.EXTRA_ACCURACY, 0);
                        double altitude = intent.getDoubleExtra(LocationService.EXTRA_ALTITUDE, 0);
                        float bearing = intent.getFloatExtra(LocationService.EXTRA_BEARING, 0);
                        float speed = intent.getFloatExtra(LocationService.EXTRA_SPEED, 0);

                        Date date = new Date(time);
                        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G, HH:mm:ss z");
                        String formattedTime = dateFormat.format(date);

                        textView.append("Provider: " + provider + "; Latitude: " + latitude + "; Longitude: " + longitude + "; Time: " + formattedTime + "; Elapsed realtime nanos: " + elapsedRealtimeNanos + "; Extras: " + extras + "; Accuracy: " + accuracy + "; Altitude: " + altitude + "; Bearing: " + bearing + "; Speed: " + speed + "; \n\n");
                    }
                }, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );

        final Button startButton = (Button) findViewById(R.id.content_main_start_button);
        startButton.setVisibility(View.VISIBLE);

        final Button stopButton = (Button) findViewById(R.id.content_main_stop_button);
        stopButton.setVisibility(View.INVISIBLE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Configuration.sIsFeatureLocationAvailable) {
                    LogHelper.debugLog("Starting \"" + LocationService.class.getName() + "\"");
                    try {
                        startService(new Intent(MainActivity.this, LocationService.class));
                    } catch (Exception e) {
                        LogHelper.errorLog("Error while trying to start \"" + LocationService.class.getName() + "\"");
                        e.printStackTrace();
                    }

                    LogHelper.debugLog("Binding \"" + LocationService.class.getName() + "\"");
                    try {
                        bindService(new Intent(MainActivity.this, LocationService.class), mLocationServiceConnection, Context.BIND_AUTO_CREATE);
                    } catch (Exception e) {
                        LogHelper.errorLog("Error while trying to bind \"" + LocationService.class.getName() + "\"");
                        e.printStackTrace();
                    }
                }

                startButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Configuration.sIsFeatureLocationAvailable) {
                    LogHelper.debugLog("Unbinding \"" + LocationService.class.getName() + "\"");
                    try {
                        unbindService(mLocationServiceConnection);
                    } catch (Exception e) {
                        LogHelper.errorLog("Error while trying to unbind \"" + LocationService.class.getName() + "\"");
                        e.printStackTrace();
                    }

                    LogHelper.debugLog("Stopping \"" + LocationService.class.getName() + "\"");
                    try {
                        stopService(new Intent(MainActivity.this, LocationService.class));
                    } catch (Exception e) {
                        LogHelper.errorLog("Error while trying to stop \"" + LocationService.class.getName() + "\"");
                        e.printStackTrace();
                    }
                }

                stopButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogHelper.verboseLog("\"" + this.getClass().getName() + "\" onStart");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            for (final String permission : permissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(permissions, 1);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogHelper.debugLog("\"" + this.getClass().getName() + "\" onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogHelper.debugLog("\"" + this.getClass().getName() + "\" onOptionsItemSelected");

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
