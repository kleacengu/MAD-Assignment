package uk.ac.solent.pointsofinterest;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationListener {

    MapView mv;
    ItemizedIconOverlay.OnItemGestureListener<OverlayItem> markerGestureListener;
    ItemizedIconOverlay<OverlayItem> items;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This line sets the user agent, a requirement to download OSM maps
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location loc = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // if (loc == null)  loc = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        // if (loc == null)  loc = mgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        mv = findViewById(R.id.map1);
        mv.setBuiltInZoomControls(true);
        mv.getController().setZoom(14);
        if (loc == null) {
            mv.getController().setCenter(new GeoPoint(51.05, -0.72));
        } else {
            mv.getController().setCenter(new GeoPoint(loc.getLatitude(), loc.getLongitude()));

            //mv.getMapCenter() - GeoPoint representing the current map centre position.
        }
    }


    public void onLocationChanged(Location newLoc) {
        mv = findViewById(R.id.map1);
        mv.getController().setCenter(new GeoPoint(newLoc.getLatitude(), newLoc.getLongitude()));
        Toast.makeText
                (this, "Location=" +
                        newLoc.getLatitude() + " " +
                        newLoc.getLongitude(), Toast.LENGTH_LONG).show();
    }

    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provider " + provider +
                " disabled", Toast.LENGTH_LONG).show();
    }

    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Provider " + provider +
                " enabled", Toast.LENGTH_LONG).show();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

        Toast.makeText(this, "Status changed: " + status,
                Toast.LENGTH_LONG).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addPoint) {
            Intent intent = new Intent(this, PointsActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        if (item.getItemId() == R.id.savePoint) {
            POIList.save();
            return true;
        }
        if (item.getItemId() == R.id.loadPoint) {
            POIList.load();
            addmarkers();
            return true;
        }
        if (item.getItemId() == R.id.pref) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        }

        return false;
    }

    class MyMarkerGestureListener implements ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
        public boolean onItemLongPress(int i, OverlayItem item) {
            Toast.makeText(MainActivity.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
            return true;
        }

        public boolean onItemSingleTapUp(int i, OverlayItem item) {
            Toast.makeText(MainActivity.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                POI poi = new POI();
                MapView mv = findViewById(R.id.map1);
                Bundle extras = intent.getExtras();
                String name = extras.getString("newname");
                String type = extras.getString("newtype");
                String description = extras.getString("newdescription");

                // Assign the return value of mv.getMapCenter() to a GeoPoint object.

                Double lat = mv.getMapCenter().getLatitude();
                Double lon = mv.getMapCenter().getLongitude();
                poi.setName(name);
                poi.setType(type);
                poi.setDescription(description);
                poi.setLatitude(lat);
                poi.setLongitude(lon);

                Log.d("assignment", "Lat=" + lat + " Lon=" + lon);
                POIList.addPOI(poi);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                boolean autoupload = prefs.getBoolean("autoupload", false);
                if (autoupload) {
                    MyTask t = new MyTask();
                    t.execute(poi);
                }

                addmarkers();

            }
        }
    }

    private void addmarkers() {
        markerGestureListener = new MyMarkerGestureListener();


        items = new ItemizedIconOverlay<OverlayItem>(this, new ArrayList<OverlayItem>(), markerGestureListener);
        for (POI poi : POIList.getPoiList()) {
            System.out.println("adding poi to items:" + poi);
            OverlayItem marker = new OverlayItem(poi.getName(), poi.getType(), poi.getDescription(), new GeoPoint(poi.getLatitude(), poi.getLongitude()));
            items.addItem(marker);
        }
        mv.getOverlays().clear();
        mv.getOverlays().add(items);
    }

    public void onResume() {


        super.onResume();
    }

    public void onDestroy() {
        POIList.save();
        super.onDestroy();
    }

    class MyTask extends AsyncTask<POI, Void, String> {

        public String doInBackground(POI... poi) {
            MapView mv = findViewById(R.id.map1);

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://www.free-map.org.uk/course/mad/ws/add.php");
                conn = (HttpURLConnection) url.openConnection();


                String name = poi[0].getName();
                String type = poi[0].getType();
                String description = poi[0].getDescription();
                Double lat = poi[0].getLatitude();
                Double lon = poi[0].getLongitude();
                String postData = "name=" + name + "&type=" + type + "&description=" + description + "&lat=" + lat + "&lon=" + lon + "&username=user012";
                // For POST
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(postData.length());

                OutputStream out = null;
                out = conn.getOutputStream();
                out.write(postData.getBytes());
                if (conn.getResponseCode() == 200) {
                    InputStream in = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String all = "", line;
                    while ((line = br.readLine()) != null)
                        all += line;
                    return all;
                } else {
                    return "HTTP ERROR: " + conn.getResponseCode();
                }
            } catch (IOException e) {
                System.out.println("****************************************  debug " + e);
                return e.toString();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        public void onPostExecute(String result) {
            new AlertDialog.Builder(MainActivity.this).
                    setMessage("Uploaded POI: " + result).
                    setPositiveButton("OK", null).show();
        }
    }


}

