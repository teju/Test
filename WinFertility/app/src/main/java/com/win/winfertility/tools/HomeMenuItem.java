package com.win.winfertility.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.win.winfertility.R;

public class HomeMenuItem extends RelativeLayout {
    private ImageView _vw_image;
    private TextView _vw_text;

    public HomeMenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }
    public HomeMenuItem(Context context) {
        super(context);
        this.init(context, null);
    }

    public void setText(String text) {
        if(this._vw_text != null) {
            this._vw_text.setText(text);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null) {
            inflater.inflate(R.layout.view_home_menu_item, this);
        }

        this.setClickable(true);
        this.setFocusable(true);
        this._vw_image = (ImageView) this.findViewById(R.id.vw_image);
        this._vw_image.setColorFilter(Color.parseColor("#FFFFFF"));
        this._vw_text = (TextView) this.findViewById(R.id.vw_text);

        if(attrs != null) {
            if(this._vw_image != null && this._vw_text != null) {
                TypedArray items = context.obtainStyledAttributes(attrs, R.styleable.WinAppStyleable, 0, 0);
                try {
                    /*----- Setting Text -----*/
                    String text = items.getString(R.styleable.WinAppStyleable_text);
                    this._vw_text.setText(text);
                    /*----- Setting Image Source -----*/
                    Drawable src = items.getDrawable(R.styleable.WinAppStyleable_src);
                    this._vw_image.setImageDrawable(src);
                } catch (Exception ex) {
                }
                items.recycle();
            }
        }

        final HomeMenuItem pointer = this;
        View vw_overlay = this.findViewById(R.id.vw_overlay);
        if(vw_overlay != null) {
            vw_overlay.setClickable(true);
            vw_overlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    pointer.callOnClick();
                }
            });
            vw_overlay.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        pointer._vw_image.setAlpha(.2f);
                        pointer.setAlpha(0.8f);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        pointer._vw_image.setAlpha(.5f);
                        pointer.setAlpha(1f);
                    }
                    return false;
                }
            });
        }
    }
}
