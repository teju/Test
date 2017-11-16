package com.win.winfertility;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.EmployerContactManager;
import com.win.winfertility.utils.RichTextInputManager;
import com.win.winfertility.utils.GraphManager;
import com.win.winfertility.utils.NotificationManager;
import com.win.winfertility.utils.Shared;

public class SettingsActivity extends WINFertilityActivity {
    private SettingsActivity Pointer;

    private ImageView _img_profile;
    private GraphManager _graphManager;
    private NotificationManager _notificationManager;
    private RichTextInputManager _feedbackManager;
    private EmployerContactManager _employerContactManager;
    private RelativeLayout _notifications;
    private ScrollView _linear_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
        Common.hideKeyboard(this);
        this.Pointer = this;
        this.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LandingActivity landingActivity=new LandingActivity();
        landingActivity.handleFCMData();
        if(Pointer._img_profile != null) {
            Pointer._img_profile.setImageBitmap(Common.getProfileImage(Pointer));
        }
    }

    private void init() {
        Shared.setString(this, Shared.KEY_LEGAL_URL, "http://www.winfertility.com/terms-and-conditions-of-use-and-privacy-statement/");
        Pointer._img_profile = (ImageView) this.findViewById(R.id.img_profile);
        Pointer._linear_list = (ScrollView) this.findViewById(R.id.linear_list);
        if(Pointer._img_profile != null) {
            Pointer._img_profile.setImageBitmap(Common.getProfileImage(Pointer));
        }

        View.OnClickListener menuOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                Intent intent = null;

                switch (id) {

                    case R.id.mnu_cycle_settings:
                        intent = new Intent(Pointer, MenstrualInfoActivity.class);
                        intent.putExtra(Shared.EXTRA_PARENT_CLASS, SettingsActivity.class.getName());
                        Pointer.startActivity(intent);
                        break;
                    case R.id.mnu_graphs:
                        Pointer._graphManager.showDialog();
                        break;
                    case R.id.mnu_reminders:
                        Pointer.startActivity(new Intent(Pointer, MyFertilityActivity.class));
                        break;
                    case R.id.mnu_support:
                        Pointer.startActivity(new Intent(Pointer, ContactActivity.class));
                        break;
                    case R.id.mnu_edit_profile:
                        Pointer.startActivity(new Intent(Pointer, ViewProfileActivity.class));
                        break;
                    case R.id.mnu_change_pwd:
                        Pointer.startActivity(new Intent(Pointer, ChangePasswordActivity.class));
                        break;
                    case R.id.mnu_feedback:
                        Pointer._feedbackManager.showDialog(true);
                        break;
                    case R.id.mnu_legal:
                        intent = new Intent(Pointer, BrowserActivity.class);
                        intent.putExtra("URL", Shared.KEY_LEGAL_URL);
                        startActivity(intent);
                        break;
                    case R.id.mnu_contact_employer:
                        Pointer._employerContactManager.showDialog();
                        break;
                    case R.id.mnu_turn_off_notifications:
                        System.out.println("ONCLICK WORKING! "+id);
                        intent = new Intent(Pointer, Notifications.class);
                        startActivity(intent);
                        break;
                    case R.id.mnu_logout:
                        Common.logOut(Pointer);
                        break;
                }
            }
        };

        View mnu_edit_profile = Pointer.findViewById(R.id.mnu_edit_profile);
        if(mnu_edit_profile != null) {
            mnu_edit_profile.setOnClickListener(menuOnClickListener);
        }

        View mnu_turn_off_notifications = Pointer.findViewById(R.id.mnu_turn_off_notifications);
        if(mnu_turn_off_notifications != null) {
            System.out.println("ONCLICK nonull!");
            mnu_turn_off_notifications.setOnClickListener(menuOnClickListener);
        }

        View mnu_cycle_settings = Pointer.findViewById(R.id.mnu_cycle_settings);
        if(mnu_cycle_settings != null) {
            mnu_cycle_settings.setOnClickListener(menuOnClickListener);
        }


        View mnu_graphs = Pointer.findViewById(R.id.mnu_graphs);
        if(mnu_graphs != null) {
            mnu_graphs.setOnClickListener(menuOnClickListener);
        }

        View mnu_reminders = Pointer.findViewById(R.id.mnu_reminders);
        if(mnu_reminders != null) {
            mnu_reminders.setOnClickListener(menuOnClickListener);
        }

        View mnu_contact_employer = Pointer.findViewById(R.id.mnu_contact_employer);
        if(mnu_contact_employer != null) {
            mnu_contact_employer.setOnClickListener(menuOnClickListener);
        }

        View mnu_support = Pointer.findViewById(R.id.mnu_support);
        if(mnu_support != null) {
            mnu_support.setOnClickListener(menuOnClickListener);
        }

        View mnu_legal = Pointer.findViewById(R.id.mnu_legal);
        if(mnu_legal != null) {
            mnu_legal.setOnClickListener(menuOnClickListener);
        }

        View mnu_feedback = Pointer.findViewById(R.id.mnu_feedback);
        if(mnu_feedback != null) {
            mnu_feedback.setOnClickListener(menuOnClickListener);
        }

        View mnu_change_pwd = Pointer.findViewById(R.id.mnu_change_pwd);
        if(mnu_change_pwd != null) {
            mnu_change_pwd.setOnClickListener(menuOnClickListener);
        }

        View mnu_logout = Pointer.findViewById(R.id.mnu_logout);
        if(mnu_logout != null) {
            mnu_logout.setOnClickListener(menuOnClickListener);
        }

        TextView lbl_profile_name = (TextView) Pointer.findViewById(R.id.lbl_profile_name);
        if(lbl_profile_name != null) {
            lbl_profile_name.setText(Shared.getString(Pointer, Shared.KEY_PROFILE_NAME));
        }

        /*----- Popup Manager Functionality -----*/
        Pointer._graphManager = new GraphManager(this);
        Pointer._graphManager.init(Pointer._linear_list);

        Pointer._feedbackManager = new RichTextInputManager(this);
        Pointer._feedbackManager.init(_linear_list);

        Pointer._employerContactManager = new EmployerContactManager(this);
        Pointer._employerContactManager.init(_linear_list);

        Pointer._notificationManager = new NotificationManager(this);
        Pointer._notificationManager.init();
    }



}
