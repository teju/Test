package com.bikerservice.biker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bikerservice.biker.Utils.Constants;
import com.bikerservice.biker.Utils.CustomToast;
import com.bikerservice.biker.Utils.IsNetworkConnection;
import com.bikerservice.biker.Utils.PrintClass;
import com.bikerservice.biker.Utils.post_async;
import com.bikerservice.biker.model.PlaceJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener , OnMapReadyCallback {
    private LatLng dest;
    private ArrayList<LatLng> markerPoints;
    private GoogleMap map;
    private LocationManager locationManager;
    private EditText vehicle_no, email;
    private View rootView;
    private SharedPreferences prefrence;
    private int backpress = 0;
    double getLatitude = 0, getLongitude = 0;
    String getAddress = "Not Found";
    private SharedPreferences.Editor editor;
    TextView profile_name;
    private Button search_button;
    float zoom_val = 15;
    private SupportMapFragment fm;
    private ImageView imageView;
    private Marker marker;
    private ArrayAdapter<String> adapter;
    private AutoCompleteTextView auto_tv;
    private ArrayList<String> names;
    private static final String TAG_RESULT = "predictions";
    private Dialog dialog;
    private String vehicle_no_str;
    private GPSTracker gps;
    static final int _requestLocation = 1;
    private ImageView noti;
    private ImageView noti_indication;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.spinner);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getAddress = "";
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        //  Constants.statusColor(this);
        rootView = findViewById(android.R.id.content);
        //editText = (EditText) findViewById(R.id.editText);
        auto_tv = (AutoCompleteTextView) findViewById(R.id.editText);
        auto_tv.setThreshold(0);
        search_button = (Button) findViewById(R.id.search_button);
        fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN);
        RelativeLayout gps_icon = (RelativeLayout) findViewById(R.id.gps_icon);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        profile_name = (TextView) header.findViewById(R.id.profile_name);
        noti = (ImageView)findViewById(R.id.noti);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);


        noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this, Notifications.class);
                startActivity(i);
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getLatLong(zoom_val);
            }
        }
        auto_tv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(auto_tv.getText().toString().trim().length() != 0) {
                        dialog.show();
                        if(marker != null){
                            marker.remove();
                        }
                        getLatLogFromAddress(auto_tv.getText().toString());

                    } else {
                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                "Enter the name of place you want to search");
                    }
                    return true;
                }
                return false;
            }
        });
        gps_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue),
                                android.graphics.PorterDuff.Mode.SRC_IN);


                        if (auto_tv.getText().toString().length() == 0) {
                            getLatLong(zoom_val + 4);
                        } else {
                            if (marker != null) {
                                marker.remove();
                            }
                            showMap(getLatitude, getLongitude, zoom_val + 4);
                        }
                    }
                }
            }
        });
        names = new ArrayList<String>();
       /* auto_tv.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    PlacesTask placesTask = new PlacesTask();
                    placesTask.execute(v.getText().toString());
                    return true;
                }
                return false;
            }
        });*/
        auto_tv.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                if (s.toString().length() != 0) {
                    names = new ArrayList<String>();
                    PlacesTask placesTask = new PlacesTask(dialog);
                    placesTask.execute(s.toString());
                    //dialog.show();
                }
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(auto_tv.getText().toString().trim().length() != 0) {
                    dialog.show();
                    if(marker != null){
                        marker.remove();
                    }
                    getLatLogFromAddress(auto_tv.getText().toString());

                    /*names = new ArrayList<String>();
                    PlacesTask placesTask = new PlacesTask(MainActivity.this);
                    placesTask.execute(auto_tv.getText().toString());*/
                    //search();
                } else {
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "Enter the name of place you want to search");
                }
            }
        });

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            showMap(getLatitude,getLongitude,zoom_val);

            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.black),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                    if(auto_tv.getText().toString().length() == 0) {
                        getLatLong(zoom_val + 2);
                    } else {
                        if(marker != null){
                            marker.remove();
                        }
                        showMap(getLatitude,getLongitude,zoom_val);
                    }
                }
            });
        }

    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch(Exception e){
            dialog.cancel();
            PrintClass.printValue("jsonObjReqMAinactivity Exception ",e.toString());

        } finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    private class PlacesTask extends AsyncTask<String, Void, String>{

        private Dialog dialog;

        public PlacesTask(Dialog dialog) {
            this.dialog=dialog;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyDQqHe9i8DSWl7vGtJixki8KftHUuUDChM";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                dialog.cancel();
                PrintClass.printValue("jsonObjReqMAinactivity UnsupportedEncodingException ",e1.toString());

                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
            PrintClass.printValue("jsonObjReqMAinactivity url ",url);

            try {
                // Fetching the data from we service
                data = downloadUrl(url);
            }catch(Exception e){
                dialog.cancel();
                PrintClass.printValue("jsonObjReqMAinactivity Exception ",e.toString());

                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
                dialog.dismiss();
            // Creating ParserTask
            ParserTask parserTask = new ParserTask(dialog);

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {

        private final Dialog dialog;
        JSONObject jObject;

        public ParserTask(Dialog dialog) {
            this.dialog=dialog;

        }
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);
                PrintClass.printValue("jsonObjReqMAinactivity jObject ",jObject.toString());

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch(Exception e){
                dialog.cancel();
                PrintClass.printValue("jsonObjReqMAinactivity Exception ",e.toString());

                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);
            PrintClass.printValue("jsonObjReqMAinactivity from ", from.toString()+" result "+result.size());
            if(result.size() == 0) {

                dialog.cancel();
                return;
            }
            dialog.dismiss();
            // Setting the adapter
            auto_tv.setAdapter(adapter);
            auto_tv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                    dialog.show();
                    auto_tv.setText(((TextView) view).getText().toString());
                    getLatLogFromAddress((String) ((TextView) view).getText());
                }
            });
        }
    }

    public void getLatLogFromAddress(final String address) {
        Thread t =new Thread(new Runnable() {
            @Override
            public void run() {
                PrintClass.printValue("SEARCHAUTOCOMPLETE address ", address);
                Geocoder coder = new Geocoder(MainActivity.this);
                try {
                    List<android.location.Address> adresses =  coder.getFromLocationName(address, 50);
                    for(android.location.Address add : adresses){
                        getLongitude = add.getLongitude();
                        getLatitude = add.getLatitude();
                        PrintClass.printValue("SEARCHAUTOCOMPLETE address "," longitude"+ getLatitude+ " latitude " +getLongitude);
                        getAddress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(marker != null){
                                    marker.remove();
                                }
                                auto_tv.setText(getAddress);
                                showMap(getLatitude,getLongitude,15);

                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    dialog.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getLatLong(zoom_val);
                        }
                    });
                }
            //    final JSONObject ret = getLocationInfo(0,0, address);
/*
                try {
                    try {
                        PrintClass.printValue("SEARCHAUTOCOMPLETE "," getAddress " +((JSONArray) ret.get("results")));
                        final double longitute = ((JSONArray) ret.get("results")).getJSONObject(0)
                                .getJSONObject("geometry").getJSONObject("location")
                                .getDouble("lng");

                        final double latitude = ((JSONArray)ret.get("results")).getJSONObject(0)
                                .getJSONObject("geometry").getJSONObject("location")
                                .getDouble("lat");
                        getLatitude=latitude;
                        getLongitude=longitute;
                        getAddress=((JSONArray) ret.get("results")).getJSONObject(0)
                                .getString("formatted_address").toString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(marker != null){
                                    marker.remove();
                                }
                                auto_tv.setText(getAddress);
                                showMap(latitude,longitute,15);

                            }
                        });

                    } catch (JSONException e) {
                        dialog.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getLatLong(zoom_val);
                            }
                        });
                        PrintClass.printValue("SEARCHAUTOCOMPLETE ","JSONException"  +e.toString());
                    }
                } catch (Exception e1) {
                    dialog.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            getLatLong(zoom_val);
                        }
                    });
                    e1.printStackTrace();
                    PrintClass.printValue("SEARCHAUTOCOMPLETE ","Exception " +e1.toString());
                }
*/
            }
        });
        t.start();

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

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
                            getLatLong(zoom_val);
                    }

                } else {

                }
                return;
            }
        }
    }

    public void getLatLong(float zoom_val){
        gps = new GPSTracker(this);
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        } else {
            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();
            getLatitude=latitude;
            getLongitude=longitude;
            getAddress();
            if(marker != null){
                marker.remove();
            }
                showMap(latitude,longitude,zoom_val);
            }
    }

    public JSONObject getLocationInfo(double lat,double lng,String address) {
        HttpGet httpGet = null;
        if(lat != 0 &&  lng != 0) {
            httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true");
        } else {
            try {
                httpGet = new HttpGet("http://maps.googleapis.com/maps/api/geocode/json?address="+URLEncoder.encode(address,"UTF-8")
                        +"&sensor=true");
                PrintClass.printValue("getLocationInfo1234 ",
                        "http://maps.googleapis.com/maps/api/geocode/json?address="+URLEncoder.encode(address,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void showMap(double latitude,double longitude,float zoom){
        dialog.dismiss();
        dest = new LatLng(latitude, longitude);
        // Initializing
        markerPoints = new ArrayList<LatLng>();
        // Getting reference to SupportMapFragment of the activity_main
        // Getting Map for the SupportMapFragment
        fm.getMapAsync(this);
        System.out.println("GOOGLEMAOP " + map);
        if (map != null) {
            System.out.println("GOOGLEMAOP not null " + map);
            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();
            // Setting the position of the marker
            options.position(dest);
            marker = map.addMarker(options);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(dest,
                    zoom));
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission. ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    auto_tv.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                PlacesTask placesTask = new PlacesTask(dialog);
                                placesTask.execute(v.getText().toString());
                                return true;
                            }
                            return false;
                        }
                    });
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView noti_count = (TextView) findViewById(R.id.noti_count);
        Constants.noti_count(this,noti_count);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN);
        PrintClass.printValue("ONRESUMEISBEING","CALLED");
        if(auto_tv.getText().toString().length()==0) {
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    getLatLong(zoom_val);
                }
            }
        }
        profile_name.setText(prefrence.getString("name", ""));

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            this.finish();

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            if (IsNetworkConnection.checkNetworkConnection(MainActivity.this)) {
                Intent i = new Intent(this, UserRegister.class);
                i.putExtra("type", "edit");
                startActivity(i);
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
            // Handle the camera action
        } else if (id == R.id.booking_history) {
            Intent i=new Intent(this,BookingDetails.class);
            startActivity(i);
        }  else if (id == R.id.booking_details) {
            Intent i=new Intent(this,BookingDetails.class);
            startActivity(i);
        } else if (id == R.id.payment_history) {
            Intent i=new Intent(this,PaymentHistory.class);
            startActivity(i);
        } else if (id == R.id.booking_completed) {
            Intent i=new Intent(this,BookingCompleted.class);
            startActivity(i);
        }/* else if (id == R.id.setting) {
            Intent i=new Intent(this,Setting.class);
            startActivity(i);
        } */else if (id == R.id.logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Logout");
            alertDialog.setMessage("Are you sure you want to Logout ?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.putString("access_token","1234");
                    editor.commit();
                    Intent i=new Intent(MainActivity.this,Login.class);
                    i.putExtra("reached_dest","false");
                    startActivity(i);
                    finish();
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getAddress () {
//        Thread t =new Thread(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject ret = getLocationInfo(getLatitude,getLongitude,"null");
//                JSONObject json_location;
//                try {
//                    System.out.println("json_locationRESULT "+ret.getJSONArray("results"));
//
//                    json_location = ret.getJSONArray("results").getJSONObject(0);
//                    getAddress = json_location.getString("formatted_address");
//                    PrintClass.printValue("LATLOGVALUE ", "LAT :" + getLatitude +
//                            " longitude " + getLongitude+" address "+getAddress);
//                    dialog.dismiss();
//
//                } catch (JSONException e1) {
//                    getAddress();
//                    e1.printStackTrace();
//                    PrintClass.printValue("LATLOGVALUE ", "JSONException  :"+e1.toString());
//                }
//            }
//        }); t.start();

        Geocoder geocoder;
        List<android.location.Address> addresses = null;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, _requestLocation);
            return;
        } else {

        }
        try {

            addresses = geocoder.getFromLocation(getLatitude, getLongitude, 1);
            if (addresses != null && addresses.size() > 0) {
                getAddress = addresses.get(0).getAddressLine(0);
                System.out.println("_latitude1234 before "+getLatitude+" _longitude "+getLongitude+" "+getAddress);

            }
        } catch (IOException e) {
            System.out.println("_latitude1234 IOException "+e.toString());

        }
    }


    public void Submit(View view) {
       // dialog.show();
        getAddress();
//        if (getAddress.length() != 0) {
            final Dialog mBottomSheetDialog = new Dialog(this);
            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBottomSheetDialog.setContentView(R.layout.books_service);
            mBottomSheetDialog.setCancelable(true);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.show();
            Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
            vehicle_no = (EditText) mBottomSheetDialog.findViewById(R.id.vehicle_no);
            vehicle_no.setText(prefrence.getString("vehicle_no", ""));
            email = (EditText) mBottomSheetDialog.findViewById(R.id.email);
            email.setText(prefrence.getString("email", ""));
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (validate()) {
                        if (IsNetworkConnection.checkNetworkConnection(MainActivity.this)) {

                            String url = Constants.SERVER_URL + "booking/request";

                            JSONObject jsonBody = new JSONObject();
                            JSONObject params = new JSONObject();
                            JSONObject params2 = new JSONObject();
                            try {
                                vehicle_no_str = vehicle_no.getText().toString();
                                params.put("email_id", email.getText().toString());
                                editor.putString("email",email.getText().toString());
                                editor.commit();
                                params.put("vehicle_no", vehicle_no.getText().toString());
                                params2.put("address", getAddress);
                                params2.put("lattitude", String.valueOf(getLatitude));
                                params2.put("longitude", String.valueOf(getLongitude));
                                jsonBody.put("Booking", params);
                                jsonBody.put("Address", params2);
                                jsonBody.put("user_id", prefrence.getString("user_id", ""));
                                jsonBody.put("access_token", prefrence.getString("access_token", ""));
                                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                                new post_async(MainActivity.this, "BookingRequest").execute(url, jsonBody.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                                PrintClass.printValue("SYSTEMPRINT PARAMS Exception", e.toString());
                            }
                            mBottomSheetDialog.cancel();
                        } else {
                            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                    "No Internet Connection");
                        }
                    }
                }
            });
//        } else {
//            dialog.cancel();
//            new CustomToast().Show_Toast(getApplicationContext(), rootView,
//                    "Address Not found");
//        }
    }

    public void ResponseOfBooking(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                editor.putString("vehicle_no",vehicle_no_str);
                editor.commit();
                Intent i = new Intent(MainActivity.this, BookingSuccessful.class);
                startActivity(i);
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }
        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }


    public boolean validate(){

        String getVehicleNo=vehicle_no.getText().toString();
        String getEmail=email.getText().toString();

        Pattern p = Pattern.compile(Constants.regEx);
        Matcher m = p.matcher(getEmail);

        if(getVehicleNo.length() == 0) {
            vehicle_no.setError("Vehicle Number cannot be empty");
            return false;
        } else if(getEmail.length()==0){
            vehicle_no.setError(null);
            email.setError("Email Id cannot be empty");
            return false;
        } else if(!m.find()) {
            vehicle_no.setError(null);
            email.setError("Email Id Invalid");
            return false;

        } else if(getAddress.length() == 0) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Please switch in gps and then proceed with bookings " );
            return false;

        } else {
            vehicle_no.setError(null);
            email.setError(null);
            return  true;
        }
    }
}




