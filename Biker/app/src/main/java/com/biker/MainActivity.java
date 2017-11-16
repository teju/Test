package com.biker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.biker.Utils.Constants;
import com.biker.Utils.CustomToast;
import com.biker.Utils.IsNetworkConnection;
import com.biker.Utils.PrintClass;
import com.biker.Utils.post_async;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private LatLng dest;
    private ArrayList<LatLng> markerPoints;
    private GoogleMap map;
    private LocationManager locationManager;
    private EditText vehicle_no, email, editText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        getAddress = "";
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        //  Constants.statusColor(this);
        rootView = findViewById(android.R.id.content);
        editText = (EditText) findViewById(R.id.editText);
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
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getLatLong(zoom_val);
            }
        }

        gps_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue),
                                android.graphics.PorterDuff.Mode.SRC_IN);


                        if (editText.getText().toString().length() == 0) {
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

        if (map != null) {
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.black),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                    if(editText.getText().toString().length() == 0) {
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

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().trim().length() != 0) {
                    if(marker != null){
                        marker.remove();
                    }
                    search();
                } else {
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "Enter the name of place you want to search");
                }
            }
        });
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
        GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        } else {
            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();
            getLatitude=latitude;
            getLongitude=longitude;

            Thread t =new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject ret = getLocationInfo(latitude,longitude,"null");
                    JSONObject json_location;
                    try {
                        json_location = ret.getJSONArray("results").getJSONObject(0);
                        System.out.println("json_locationRESULT "+json_location);
                        getAddress = json_location.getString("formatted_address");
                        PrintClass.printValue("LATLOGVALUE ", "LAT :" + latitude +
                                " longitude " + longitude+" address "+getAddress);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        PrintClass.printValue("LATLOGVALUE ", "JSONException  :"+e1.toString());
                    }
                }
            }); t.start();
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
        dest = new LatLng(latitude, longitude);
        // Initializing
        markerPoints = new ArrayList<LatLng>();
        // Getting reference to SupportMapFragment of the activity_main
        // Getting Map for the SupportMapFragment
        map = fm.getMap();
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
                    editText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editText.setText("");
                            search();
                        }
                    });
                }
            }

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN);
        PrintClass.printValue("ONRESUMEISBEING","CALLED");
        if(editText.getText().toString().length()==0) {
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

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private void search() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Service Not Available");
            PrintClass.printValue("SEARCHAUTOCOMPLETE GooglePlayServicesRepairableException" +
                    " ", "" + e.toString());

            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Service Not Available");
            PrintClass.printValue("SEARCHAUTOCOMPLETE GooglePlayServicesNotAvailableException" +
                    " ", e.toString() );

            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PrintClass.printValue("SEARCHAUTOCOMPLETE requestCode ","" +requestCode);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            PrintClass.printValue("SEARCHAUTOCOMPLETE requestCode", " ok ");

            if (resultCode == RESULT_OK) {
                final Place place = PlaceAutocomplete.getPlace(this, data);
                PrintClass.printValue("SEARCHAUTOCOMPLETE ","" +place.getAddress());
                Geocoder coder = new Geocoder(this);
                List<Address> address;
                editText.setText(place.getAddress());
                getAddress =place.getAddress().toString();
                Thread t =new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject ret = getLocationInfo(0,0, (String) place.getAddress().toString());
                        try {
                            try {

                                final double longitute = ((JSONArray) ret.get("results")).getJSONObject(0)
                                        .getJSONObject("geometry").getJSONObject("location")
                                        .getDouble("lng");

                                final double latitude = ((JSONArray)ret.get("results")).getJSONObject(0)
                                        .getJSONObject("geometry").getJSONObject("location")
                                        .getDouble("lat");

                                getLatitude=latitude;
                                getLongitude=longitute;
                                PrintClass.printValue("SEARCHAUTOCOMPLETE "," getAddress " +getAddress
                                        +" latitude "+getLatitude+" longitude "+getLongitude);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(marker != null){
                                            marker.remove();
                                        }
                                        showMap(latitude,longitute,15);

                                    }
                                });

                            } catch (JSONException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                                "Address Not Found");
                                        getLatLong(zoom_val);

                                    }
                                });

                                PrintClass.printValue("SEARCHAUTOCOMPLETE ","JSONException"  +e.toString());
                            }
                        } catch (Exception e1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                            "Address Not Found");
                                    getLatLong(zoom_val);
                                }
                            });
                            e1.printStackTrace();
                            PrintClass.printValue("SEARCHAUTOCOMPLETE ","Exception " +e1.toString());
                        }
                    }
                });t.start();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                PrintClass.printValue("SEARCHAUTOCOMPLETE ","" +status.getStatusMessage());
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Address Not Found");
                getLatLong(zoom_val);

            } else if (resultCode == RESULT_CANCELED) {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Address Not Found");
                getLatLong(zoom_val);
            }
        }
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
                Intent i=new Intent(this,ServerError.class);
                startActivity(i);
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




    public void Submit(View view) {
        if (getAddress.length() != 0) {
            final Dialog mBottomSheetDialog = new Dialog(this);
            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBottomSheetDialog.setContentView(R.layout.books_service);
            mBottomSheetDialog.setCancelable(true);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.show();
            Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
            vehicle_no = (EditText) mBottomSheetDialog.findViewById(R.id.vehicle_no);
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
                                params.put("email_id", email.getText().toString());
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
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Location Not Found");
            alertDialog.setMessage("Make sure your GPS is enabled to book your service");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                   dialog.dismiss();
                }
            });

            alertDialog.show();
        }
    }

    public void ResponseOfBooking(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
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

        } else {
            vehicle_no.setError(null);
            email.setError(null);
            return  true;
        }
    }
}




