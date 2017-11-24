package com.vendor.biker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.GPSTracker;
import com.vendor.biker.Utils.IsNetworkConnection;
import com.vendor.biker.Utils.PrintClass;
import com.vendor.biker.Utils.post_async;
import com.vendor.biker.model.PathJSONParser;

public class PathGoogleMapActivity extends FragmentActivity {

    private static LatLng origin ;
    private static LatLng dest ;

    GoogleMap googleMap;
    private LocationManager locationManager;
    double dest_latitude=0;
    double dest_longitude=0;
    private View rootView;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    private String booking_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_google_map);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();
        dest_latitude=Double.parseDouble(getIntent().getStringExtra("latitude"));
        dest_longitude=Double.parseDouble(getIntent().getStringExtra("longitude"));
        booking_id=getIntent().getStringExtra("booking_id");
        PrintClass.printValue("PathGoogleMapActivity latitude ",dest_latitude+" longitude "+dest_longitude);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        rootView=findViewById(android.R.id.content);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();

        try {
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission. ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    getLatLong();
                }
            }
        } catch (Exception e){
            PrintClass.printValue("PathGoogleMapActivity Exception ",e.toString());
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    public void deliver(View view){
        if (IsNetworkConnection.checkNetworkConnection(PathGoogleMapActivity.this)) {
            String url = Constants.SERVER_URL + "vendor/reached-destination";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", ""));
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("booking_id",booking_id);
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(PathGoogleMapActivity.this,"Deliver").execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission. ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        getLatLong();
                    }

                } else {

                }
                return;
            }
        }
    }

    public void getLatLong(){
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Yout GPS seems to be disabled, do you want to enable it?");
            alert.setPositiveButton("Back", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("Go to Settings", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Intent I = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(I);

                }
            });
            AlertDialog al_gps = alert.create();
            al_gps.show();
        } else {
            GPSTracker gps = new GPSTracker(this);
            if(!gps.canGetLocation()){
                gps.showSettingsAlert();
            } else {
                final double latitude = gps.getLatitude();
                final double longitude = gps.getLongitude();
                showMap(latitude,longitude);
            }
        }
    }

    private void showMap(double latitude, double longitude) {
        MarkerOptions options = new MarkerOptions();
        origin=new LatLng(latitude, longitude);
        dest=new LatLng(dest_latitude,dest_longitude);
        options.position(origin);
        options.position(dest);
        googleMap.addMarker(options);
        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));
        addMarkers();
    }

    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

            }
            Location l = locationManager.getLastKnownLocation(provider);
            PrintClass.printValue("LATLOGVALUE ", "last known location, provider: %s, location: %s" + provider +
                    l);
            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                PrintClass.printValue("LATLOGVALUE ", "found best last known location: %s" + l);
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    private String getMapsApiDirectionsUrl() {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        PrintClass.printValue("PathGoogleMapActivity getMapsApiDirectionsUrl ","url "+url);

        return url;
    }

    private void addMarkers() {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(origin)
                    .title("First Point"));
            googleMap.addMarker(new MarkerOptions().position(dest)
                    .title("Second Point"));

        }
    }

    public void ResponseOfDestinationReached(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfBookingAccept resultString ", " has data " + jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                Intent i=new Intent(this,JobList.class);
                i.putExtra("booking_id", "");

                startActivity(i);
                finish();
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Successfully Accepted the booking request");
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));
            }
        } catch (Exception e){
            PrintClass.printValue("ResponseOfBookingAccept Exception ", e.toString());
        }
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strUrl) {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                PrintClass.printValue("PathGoogleMapActivity Exception ","  "+e.toString());

                Log.d("Exception", e.toString());
            } finally {
                try {
                    iStream.close();
                } catch (Exception e) {
                    PrintClass.printValue("PathGoogleMapActivity IOException ","  "+e.toString());
                    e.printStackTrace();
                }
                urlConnection.disconnect();
            }
            return data;
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }



    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                PrintClass.printValue("PathGoogleMapActivity ParserTask Exception "," "+e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            try {
                ArrayList<LatLng> points = new ArrayList<>();
                PolylineOptions polyLineOptions = null;
                PrintClass.printValue("PathGoogleMapActivity ParserTask onPostExecute ", "SIZE" + routes.size());

                // traversing through routes
                for (int i = 0; i < routes.size(); i++) {
                    points = new ArrayList<LatLng>();
                    polyLineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = routes.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    polyLineOptions.addAll(points);
                    polyLineOptions.width(8);
                    polyLineOptions.color(Color.BLUE);
                }

                googleMap.addPolyline(polyLineOptions);
            }catch (Exception e){
                PrintClass.printValue("PathGoogleMapActivity onPostExecute" +
                        " Exception ", "" + e.toString());

            }
        }
    }

}