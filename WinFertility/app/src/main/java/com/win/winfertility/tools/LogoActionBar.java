package com.win.winfertility.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.win.winfertility.R;

public class LogoActionBar extends RelativeLayout {
    private View _btn_back;
    private View _btn_logo;

    public LogoActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }
    public LogoActionBar(Context context) {
        super(context);
        this.init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null) {
            inflater.inflate(R.layout.view_logo_actionbar, this);
        }

        this._btn_back = this.findViewById(R.id.btn_back);
        this._btn_logo = this.findViewById(R.id.btn_logo);

        if(attrs != null) {
            if(this._btn_back != null) {
                this._btn_back.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ((Activity) getContext()).onBackPressed();
                        return true;
                    }
                });

                TypedArray items = context.obtainStyledAttributes(attrs, R.styleable.WinAppStyleable, 0, 0);
                try {
                    boolean hide_back = items.getBoolean(R.styleable.WinAppStyleable_hide_back, false);
                    this._btn_back.setVisibility(hide_back ? View.GONE : View.VISIBLE);
                } catch (Exception ex) {
                }
                items.recycle();
            }
        }
    }
}
