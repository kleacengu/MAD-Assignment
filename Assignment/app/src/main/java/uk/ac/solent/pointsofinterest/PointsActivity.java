package uk.ac.solent.pointsofinterest;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

public class PointsActivity extends AppCompatActivity implements View.OnClickListener {





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.points);

        // This line sets the user agent, a requirement to download OSM maps
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Button regular = (Button)findViewById(R.id.btn1);
        regular.setOnClickListener(this);
    }




    public void onClick(View v) {
        LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Intent intent = new Intent();
        Bundle bundle = new Bundle();

            final EditText et1 = (EditText) findViewById(R.id.et1);
            final EditText et2 = (EditText) findViewById(R.id.et2);
            final EditText et3 = (EditText) findViewById(R.id.et3);

            System.out.println("debug adding poi :" + toString());


        bundle.putString("newname", et1.getText().toString() );
        bundle.putString("newtype", et2.getText().toString());
        bundle.putString("newdescription", et3.getText().toString());

        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
        finish();

    }
}

