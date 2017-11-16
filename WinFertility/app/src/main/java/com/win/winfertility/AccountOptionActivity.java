package com.win.winfertility;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ProfileDisplayData;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.Shared;

public class AccountOptionActivity extends WINFertilityActivity {
    public static AccountOptionActivity Instance;

    private AccountOptionActivity Pointer;
    private RadioButton _rb_option_a;
    private RadioButton _rb_option_b;
    private ProfileDisplayData _data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccountOptionActivity.Instance = this;
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_account_option);
        this.Pointer = this;
        this.init();
        this.handleIntentData();
    }

    private void init() {
        final View vw_option_a = this.findViewById(R.id.vw_option_a);
        final View vw_option_b = this.findViewById(R.id.vw_option_b);

        Pointer._rb_option_a = (RadioButton) this.findViewById(R.id.rb_option_a);
        Pointer._rb_option_b = (RadioButton) this.findViewById(R.id.rb_option_b);

        if(vw_option_a != null) {
            vw_option_a.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(Pointer._rb_option_a != null) {
                        Pointer._rb_option_a.setChecked(true);
                    }
                    if(Pointer._rb_option_b != null) {
                        Pointer._rb_option_b.setChecked(false);
                    }
                    return true;
                }
            });
        }

        if(vw_option_b != null) {
            vw_option_b.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(Pointer._rb_option_b != null) {
                        Pointer._rb_option_b.setChecked(true);
                    }
                    if(Pointer._rb_option_a != null) {
                        Pointer._rb_option_a.setChecked(false);
                    }
                    return true;
                }
            });
        }

        View btn_next = this.findViewById(R.id.btn_next);
        if(btn_next != null) {
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Pointer.handleNext();
                }
            });
        }
    }
    private void handleIntentData() {
        Intent intent = this.getIntent();
        if(intent != null) {
            if(intent.hasExtra("DATA")) {
                String json = intent.getStringExtra("DATA");
                if(TextUtils.isEmpty(json) == false) {
                    Pointer._data = new Gson().fromJson(json, ProfileDisplayData.class);
                    if(Pointer._data != null && TextUtils.isEmpty(Pointer._data.Goal) == false) {
                        if(this._data.Goal.trim().toLowerCase().contains("pregnant")) {
                            Pointer._rb_option_a.setChecked(true);
                        }
                        else {
                            Pointer._rb_option_b.setChecked(true);
                        }
                    }
                }
            }
        }
    }
    private void handleNext() {
        if(Pointer._rb_option_a.isChecked() == false && Pointer._rb_option_b.isChecked() == false) {
            Notify.show(Pointer, "Please select any option and continue.");
            return;
        }
        /*----- Holding the selected option and moving to next screen -----*/
        Intent intent = new Intent(AccountOptionActivity.this, AccountInfoActivity.class);
        intent.putExtra(Shared.EXTRA_GOAL, (Pointer._rb_option_a.isChecked() ? "I want to get pregnant" : "I just want to track my cycle"));
        if(Pointer._data != null) {
            intent.putExtra("DATA", new Gson().toJson(Pointer._data));
        }
        Pointer.startActivity(intent);
    }
}
