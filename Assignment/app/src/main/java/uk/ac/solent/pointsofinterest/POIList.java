package uk.ac.solent.pointsofinterest;

import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class POIList {
    private static List<POI> poiList = new ArrayList<>();


    public static List<POI> getPoiList() {
        return poiList;
    }


    public static void save() {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/markers.txt"));
            for (POI poi : poiList) {
                String text = poi.getName() + "," + poi.getType() + "," + poi.getDescription() + "," + poi.getLatitude() + "," + poi.getLongitude();
                pw.println(text);
            }

            pw.close(); // close the file to ensure data is flushed to file
        } catch (IOException e) {
            System.out.println("******************** error" + e);

        } finally {
            if (pw != null) pw.close();

        }

    }

    public static void addPOI(POI poi) {
        load();
        poiList.add(poi);
        save();
    }

    //Pass in the ItemizedIconOverlay as a parameter to load()
    public static void load() {

        poiList.clear();
        BufferedReader reader = null;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/markers.txt");
            if (f.exists()) {
                FileReader fr = new FileReader(f);

                reader = new BufferedReader(fr);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    // do something with each line...
                    Log.d("assignment", "loading line: "+line);
                    String[] columns = line.split(",");
                    POI poi = new POI();

                    poi.setName(columns[0]);
                    poi.setType(columns[1]);
                    poi.setDescription(columns[2]);
                    double lat = Double.parseDouble(columns[3]);
                    poi.setLatitude(lat);
                    double lon = Double.parseDouble(columns[4]);
                    poi.setLongitude(lon);
                    Log.d("assignment", "loading line: "+line);
                    poiList.add(poi);




                }
            }

        } catch (Exception e) {
            System.out.println("******************** error " + e);


        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
            }
        }

    }
}
