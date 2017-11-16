package com.win.winfertility.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.win.winfertility.R;

import java.util.Calendar;

public class CalendarViewFlipper extends ViewFlipper {
    private float _initialX;
    private Calendar _calendar;

    public CalendarViewFlipper(Context context) {
        super(context);
    }

    public CalendarViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.handleViewFlipperTouch(event);
        return super.dispatchTouchEvent(event);
    }
    private void handleViewFlipperTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this._initialX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalX = event.getX();
                if(Math.abs(this._initialX - finalX) > 15) {
                    if (this._initialX > finalX) {
                        this.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.out_from_left));
                        this.setInAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.in_from_right));
                        this.handleCalendarData(1);
                    } else {
                        this.setInAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.in_from_left));
                        this.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.out_from_right));
                        this.handleCalendarData(-1);
                    }
                    this.showNext();
                }
                break;
        }
    }
    private void handleCalendarData(int direction) {
        if(this.getChildCount() == 2) {
            if(this._calendar == null) {
                this._calendar = Calendar.getInstance();
            }

            WinCalendarView calendar_a = (WinCalendarView) this.getChildAt(0);
            WinCalendarView calendar_b = (WinCalendarView) this.getChildAt(1);
            WinCalendarView nextCalendar;
            WinCalendarView currentCalendar = (WinCalendarView) this.getCurrentView();

            nextCalendar = (calendar_a == currentCalendar ? calendar_b : calendar_a);
            this._calendar.setTime(currentCalendar.getDate());
            this._calendar.add(Calendar.MONTH, direction);
            nextCalendar.setDate(this._calendar.getTime());
        }
    }
}
