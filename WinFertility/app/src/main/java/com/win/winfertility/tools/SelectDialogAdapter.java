package com.win.winfertility.tools;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.win.winfertility.R;
import com.win.winfertility.dto.SelectDialogItem;

import java.util.List;

public class SelectDialogAdapter extends BaseAdapter {
    private SelectDialogAdapter Pointer;

    private LayoutInflater _inflater;
    private List<SelectDialogItem> _items;
    private Context _context;
    private Handler _callback;
    private boolean _isMultiSelect;

    public SelectDialogAdapter(Context context, List<SelectDialogItem> items, Handler callback, boolean isMultiSelect) {
        this.Pointer = this;

        this._context = context;
        this._items = items;
        this._callback = callback;
        this._isMultiSelect = isMultiSelect;
    }

    @Override
    public int getCount() {
        return this._items.size();
    }
    @Override
    public SelectDialogItem getItem(int position) {
        return this._items.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            if(this._inflater == null) {
                this._inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            view = this._inflater.inflate(R.layout.view_select_dialog_item, null);
        }
        if(view != null) {
            SelectDialogItem item = this.getItem(position);
            if(item == null) {
                item = new SelectDialogItem();
            }
            view.setTag(item);

            TextView vw_text = (TextView) view.findViewById(R.id.vw_text);
            if(vw_text != null) {
                vw_text.setText(item.Text.toString());
            }
            System.out.println("SelectDialogItem123456ytttt :"+item.IsSelected+" item.ID "+item.ID);

            RadioButton rb_button = (RadioButton) view.findViewById(R.id.rb_button);
            if(rb_button != null) {
                rb_button.setChecked(item.IsSelected);
            }

            view.setOnClickListener(Pointer._onClickListener);
        }
        return view;
    }
    private View.OnClickListener _onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SelectDialogItem selected = (SelectDialogItem) v.getTag();
            RadioButton rb_button = (RadioButton) v.findViewById(R.id.rb_button);
            if(rb_button != null) {
                if(Pointer._isMultiSelect && !selected.ID.startsWith("rem")) {
                    selected.IsSelected = !selected.IsSelected;
                    rb_button.setChecked(selected.IsSelected);
                } else if(Pointer._isMultiSelect && selected.ID.startsWith("rem")) {
                    selected.IsSelected = !selected.IsSelected;
                    rb_button.setChecked(selected.IsSelected);
                    for (SelectDialogItem item : Pointer._items) {
                        item.IsSelected = (item == selected);
                    }
                    Pointer.notifyDataSetChanged();
                    selected.IsSelected = true;
                    rb_button.setChecked(selected.IsSelected);
                    if(Pointer._callback != null) {
                        Pointer._callback.sendMessage(Pointer._callback.obtainMessage(0, selected));
                    }
                } else {
                    for (SelectDialogItem item : Pointer._items) {
                        item.IsSelected = (item == selected);
                    }
                    Pointer.notifyDataSetChanged();
                    selected.IsSelected = true;
                    rb_button.setChecked(selected.IsSelected);
                    if(Pointer._callback != null) {
                        Pointer._callback.sendMessage(Pointer._callback.obtainMessage(0, selected));
                    }
                }
            }
        }
    };
}
