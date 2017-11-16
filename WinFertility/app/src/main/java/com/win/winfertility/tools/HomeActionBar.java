package com.win.winfertility.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.win.winfertility.HomeActivity;

import com.win.winfertility.MyFertilityActivity;
import com.win.winfertility.R;

public class HomeActionBar extends RelativeLayout {
    private TextView _vw_text;
    private ImageView _img_home;
    private ImageView _img_schedule;

    public HomeActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }
    public HomeActionBar(Context context) {
        super(context);
        this.init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null) {
            inflater.inflate(R.layout.view_home_actionbar, this);
        }

        this._vw_text = (TextView) this.findViewById(R.id.vw_text);
        this._img_home = (ImageView) this.findViewById(R.id.img_home);
        this._img_schedule = (ImageView) this.findViewById(R.id.img_schedule);

        if(this._img_home != null) {
            this._img_home.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Activity activity = (Activity) getContext();
                    activity.startActivity(new Intent(activity, HomeActivity.class));
                    return false;
                }
            });
        }

        if(this._img_schedule != null) {
            this._img_schedule.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Activity activity = (Activity) getContext();
                    if(MyFertilityActivity.Instance != null) {
                        MyFertilityActivity.Instance.finish();
                    }
                    activity.startActivity(new Intent(activity, MyFertilityActivity.class));
                    return false;
                }
            });
        }

        if(attrs != null) {
            if(this._vw_text != null) {
                TypedArray items = context.obtainStyledAttributes(attrs, R.styleable.WinAppStyleable, 0, 0);
                try {
                    String title = items.getString(R.styleable.WinAppStyleable_title);
                    this._vw_text.setText(title);
                } catch (Exception ex) {
                }
                items.recycle();
            }
        }
    }
    public void setText(String text) {
        if(this._vw_text != null) {
            this._vw_text.setText(text);
        }
    }
}