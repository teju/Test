package com.win.winfertility.tools;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.win.winfertility.R;
import com.win.winfertility.dto.EventItem;

import java.util.List;

public class AccordionAdapter extends BaseAdapter {
    private AccordionAdapter Pointer;
    private Context _context;
    private Handler _handler;
    private LayoutInflater _inflater;
    private CompoundButton.OnCheckedChangeListener _onCheckedChangeListener;

    private List<EventItem> _items;
    public List<EventItem> getItems() {
        return Pointer._items;
    }

    public AccordionAdapter(Context context, List<EventItem> items, Handler handler) {
        this.Pointer = this;
        this._handler = handler;
        this._context = context;
        this._items = items;

        Pointer._onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                EventItem item = (EventItem) view.getTag();
                if(item != null) {
                    item.IsSelected = isChecked;
                    if(Pointer._handler != null) {
                        Pointer._handler.sendMessage(Pointer._handler.obtainMessage(0, item));
                    }
                }
            }
        };
    }
    @Override
    public int getCount() {
        return Pointer._items.size();
    }
    @Override
    public EventItem getItem(int position) {
        return Pointer._items.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            if(Pointer._inflater == null) {
                Pointer._inflater = (LayoutInflater) Pointer._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            view = Pointer._inflater.inflate(R.layout.view_event_item, null);
        }
        if(view != null) {
            EventItem item = this.getItem(position);
            /*----- Setting Text -----*/
            TextView vw_text = (TextView) view.findViewById(R.id.vw_text);
            if(vw_text != null) {
                vw_text.setText(item.Text);
            }
            /*----- Setting toggle button status -----*/
            ToggleButton vw_toggle = (ToggleButton) view.findViewById(R.id.vw_toggle);
            if(vw_toggle != null) {
                vw_toggle.setTag(item);
                vw_toggle.setChecked(item.IsSelected);
                vw_toggle.setOnCheckedChangeListener(Pointer._onCheckedChangeListener);
            }
            /*----- Setting view events and tag data -----*/
            view.setTag(item);
        }
        return view;
    }
}
