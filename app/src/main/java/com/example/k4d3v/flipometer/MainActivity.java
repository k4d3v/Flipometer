package com.example.k4d3v.flipometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyro;
    private TextView[] speed_tvs;
    private TextView[] dist_tvs;
    private float[] dists = {0,0,0};
    private TextView[] rots_tvs; // TODO: Load rots of device
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Init. gyro
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s: sensors) {
            if (s.getType() == Sensor.TYPE_GYROSCOPE) {
                gyro = s;
                break;
            }
        }

        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        System.out.println("Gyro registered");
        
        // Init. speed
        speed_tvs = new TextView[3];
        speed_tvs[0] = ((TextView) findViewById(R.id.azimuth));
        speed_tvs[1] = ((TextView) findViewById(R.id.pitch));
        speed_tvs[2] = ((TextView) findViewById(R.id.roll));
        
        // Init. dists
        dist_tvs = new TextView[3];
        dist_tvs[0] = ((TextView) findViewById(R.id.dx));
        dist_tvs[1] = ((TextView) findViewById(R.id.dy));
        dist_tvs[2] = ((TextView) findViewById(R.id.dz));
        
        // Init. rots
        // TODO: Init. with data from device
        rots_tvs = new TextView[3];
        rots_tvs[0] = ((TextView) findViewById(R.id.rx));
        rots_tvs[1] = ((TextView) findViewById(R.id.ry));
        rots_tvs[2] = ((TextView) findViewById(R.id.rz));
        
        // Start time measurement
        time = System.nanoTime();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long tstamp = event.timestamp;
        long timestep = tstamp-time;
        // Time between events should be less than 0.1s
        if (timestep>1e8) timestep = (long) 1e8;

        // Update time
        time = event.timestamp;

        // event.values contain angular speed: up, back, side (x,y,z)
        for (int i = 0; i< speed_tvs.length; i++) {
            float speed = event.values[i];
            // Check if speed is fast enough
            if (abs(speed) > 3e-2) {
                // Update speed
                speed_tvs[i].setText(String.valueOf(Math.floor(speed*100)/100));
                
                // Increment distance
                dists[i] += speed*(timestep/1e9);
                //TODO: Reset dist if the direction was changed (Optional)
                // Show pretty number in view
                dist_tvs[i].setText(String.valueOf(Math.floor(dists[i]*100)/100));
                
                // Check for full rotation
                // TODO: Why 2 rots with 2PI???
                if (abs(dists[i])>=Math.PI) {
                    // Increment rots
                    rots_tvs[i].setText(String.valueOf(
                            Long.valueOf(rots_tvs[i].getText().toString())+1));
                    // Reset dists
                    dist_tvs[i].setText("0");
                    dists[i] = 0;
                }
            }
            // No significant movement
            else speed_tvs[i].setText("0");
        }

        // TODO: Calc distance from velocity and detect full rotation
        // TODO: Use time step
        // TODO: What exactly is rad/s?


        //R = R1 * transpose(R2)
        //angle = acos((trace(R)-1)/2)
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        System.out.println("Gyro registered");
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
