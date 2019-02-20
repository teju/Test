package com.bikerservice.biker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bikerservice.biker.R;
import com.bikerservice.biker.Utils.IsNetworkConnection;


/**
 * Created by Teju on 23/09/2017.
 */
public class Setting extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener {

    private ImageView switch_on;
    private ImageView switch_off;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    TextView profile_name;
    private ImageView noti;
    private ImageView noti_indication;

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
       // Constants.statusColor(this);

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();

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
        noti = (ImageView) header.findViewById(R.id.noti);
        noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Setting.this, Notifications.class);
                startActivity(i);
            }
        });
        profile_name=(TextView)header.findViewById(R.id.profile_name);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);

        TextView title = (TextView) findViewById(R.id.title_val);
        title.setText("Settings");

        switch_on=(ImageView)findViewById(R.id.switch_on);
        switch_off=(ImageView)findViewById(R.id.switch_off);
        switch_off.setVisibility(View.GONE);
        switch_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_on.setVisibility(View.GONE);
                switch_off.setVisibility(View.VISIBLE);
            }
        });
        switch_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_off.setVisibility(View.GONE);
                switch_on.setVisibility(View.VISIBLE);
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            // Handle the camera action
        } else if (id == R.id.profile) {
            if (IsNetworkConnection.checkNetworkConnection(Setting.this)) {
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
        } else if (id == R.id.payment_history) {
            Intent i=new Intent(this,BookingCompleted.class);
            startActivity(i);
        } else if (id == R.id.logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Logout");
            alertDialog.setMessage("Are you sure you want to Logout ?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.commit();
                    Intent i=new Intent(Setting.this,Login.class);
                    startActivity(i);
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
}
