package com.win.winfertility;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.win.winfertility.tools.HomeMenuItem;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.ContactManager;
import com.win.winfertility.utils.Shared;

public class HomeActivity extends WINFertilityActivity {
    private HomeActivity Pointer;
    private ImageView _vw_employer_logo;
    private ImageView _vw_win_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_home);
        this.Pointer = this;
        this.init();
        ContactManager.Init(this);
        //this.handlePendingScreen();
        /*----- Saving Latest Push Notification Token ID -----*/

        Common.saveFCMTokenID(Pointer, FirebaseInstanceId.getInstance().getToken());

        System.out.println("BROADLANDINF date "+"TESTING ");

        LandingActivity landingActivity=new LandingActivity();
        landingActivity.handleFCMData();
    }

    @Override
    public void onBackPressed() {
        ContactManager.Init(this);
        Common.handleAppExitMsg(Pointer);
    }

    @Override
    protected void onStop() {
        super.onStop();
       // finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContactManager.Init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResumeMethod "+"CALLED  ");
        LandingActivity landingActivity=new LandingActivity();
        landingActivity.handleFCMData();
        /*----- Loading employer logo -----*/
        if(Pointer._vw_employer_logo != null && Pointer._vw_win_logo != null) {
            Pointer._vw_win_logo.setVisibility(View.GONE);
            Pointer._vw_employer_logo.setVisibility(View.GONE);
            Bitmap bmp = Common.getEmployerLogo(Pointer);
            if(bmp != null && bmp.getWidth() > 0 && bmp.getHeight() > 0) {
                Pointer._vw_employer_logo.setImageBitmap(bmp);
                Pointer._vw_employer_logo.setVisibility(View.VISIBLE);
            }
            else {
                Pointer._vw_win_logo.setVisibility(View.VISIBLE);
            }
        }
        /*----- Call button -----*/
        try {
            HomeMenuItem mnu_coach = (HomeMenuItem) this.findViewById(R.id.mnu_coach);
            if(mnu_coach != null) {
                String enrolled = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_ENROLLED));
                if(enrolled.compareToIgnoreCase("No") == 0) {
                    mnu_coach.setText("Speak with\nWINFertility\nNurse & Enroll");
                }
                else {
                    mnu_coach.setText("Speak with\nWINFertility\nNurse");
                }
            }
        }
        catch(Exception ex) {
        }
    }

    private void init() {
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.8f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1f);
                }
                return false;
            }
        };
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mnu_fertility:
                        startActivity(new Intent(HomeActivity.this, MyFertilityActivity.class));
                        break;
                    case R.id.mnu_education:
                    case R.id.mnu_overview:
                        Intent intent = new Intent(HomeActivity.this, BrowserActivity.class);
                        if(v.getId() == R.id.mnu_education) {
                            intent.putExtra("URL", Shared.KEY_FERTILITY_EDU_URL);
                        }
                        else {
                            intent.putExtra("URL", Shared.KEY_BENEFITS_OVERVIEW_URL);
                        }
                        startActivity(intent);
                        break;
                    case R.id.mnu_search:
                        startActivity(new Intent(HomeActivity.this, ProviderSearchActivity.class));
                        break;
                    case R.id.mnu_coach:
                        startActivity(new Intent(HomeActivity.this, ContactActivity.class));
                        break;
                    case R.id.mnu_settings:
                        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                        break;
                }
            }
        };

        /*----- Bottom buttons -----*/
        View mnu_overview = this.findViewById(R.id.mnu_overview);
        if(mnu_overview != null) {
            mnu_overview.setOnTouchListener(onTouchListener);
            mnu_overview.setOnClickListener(onClickListener);
        }
        View mnu_settings = this.findViewById(R.id.mnu_settings);
        if(mnu_settings != null) {
            mnu_settings.setOnTouchListener(onTouchListener);
            mnu_settings.setOnClickListener(onClickListener);
        }
        /*----- Main Menus -----*/
        View mnu_education = this.findViewById(R.id.mnu_education);
        if(mnu_education != null) {
            mnu_education.setOnClickListener(onClickListener);
        }
        View mnu_fertility = this.findViewById(R.id.mnu_fertility);
        if(mnu_fertility != null) {
            mnu_fertility.setOnClickListener(onClickListener);
        }
        View mnu_search = this.findViewById(R.id.mnu_search);
        if(mnu_search != null) {
            mnu_search.setOnClickListener(onClickListener);
        }
        View mnu_coach = this.findViewById(R.id.mnu_coach);
        if(mnu_coach != null) {
            mnu_coach.setOnClickListener(onClickListener);
        }

        Pointer._vw_employer_logo = (ImageView) this.findViewById(R.id.vw_employer_logo);
        Pointer._vw_win_logo = (ImageView) this.findViewById(R.id.vw_win_logo);
    }
}
