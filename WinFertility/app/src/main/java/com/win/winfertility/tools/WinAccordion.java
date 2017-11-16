package com.win.winfertility.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.win.winfertility.R;
import com.win.winfertility.dto.EventItem;
import com.win.winfertility.utils.Common;

import java.util.ArrayList;
import java.util.List;

public class WinAccordion extends RelativeLayout {
    private WinAccordion Pointer;

    private TextView _vw_title;
    private ImageView _img_toggle;
    private ImageView _img_icon;
    private TextView _vw_desc;
    private View _vw_content;
    private ListView _lvw_items;
    private EditText _txt_remark;
    private View _vw_header;
    private View _vw_clickable_layer;

    private boolean _isExpanded;
    private boolean _editable;
    private boolean _isMultiSelect;
    private Handler _onEventSelectedCallback;

    public WinAccordion(Context context) {
        this(context, null);
    }
    public WinAccordion(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.Pointer = this;
        this.init(attrs);
    }

    public void setWaterMark(String text) {
        if(Pointer._txt_remark != null) {
            Pointer._txt_remark.setHint(text);
        }
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) Pointer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null) {
            inflater.inflate(R.layout.view_accordion, Pointer);
        }

        Pointer._vw_title = (TextView) Pointer.findViewById(R.id.vw_title);
        Pointer._vw_desc = (TextView) Pointer.findViewById(R.id.vw_desc);
        Pointer._img_toggle = (ImageView) Pointer.findViewById(R.id.img_toggle);
        Pointer._img_icon = (ImageView) Pointer.findViewById(R.id.img_icon);
        Pointer._vw_content = Pointer.findViewById(R.id.vw_content);
        Pointer._lvw_items = (ListView) Pointer.findViewById(R.id.lvw_items);
        Pointer._txt_remark = (EditText) Pointer.findViewById(R.id.txt_remark);
        Pointer._vw_header = Pointer.findViewById(R.id.vw_header);
        Pointer._vw_clickable_layer = Pointer.findViewById(R.id.vw_clickable_layer);

        if(Pointer._vw_clickable_layer != null) {
            Pointer._vw_clickable_layer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Pointer.setExpanded(!Pointer.isExpanded());
                    if(Pointer.isExpanded()) {
                        try {
                            ViewGroup parent = (ViewGroup) ((ViewGroup) ((Activity) getContext()).findViewById(android.R.id.content)).getChildAt(0);
                            List<WinAccordion> accordions = Common.getChildrenByType(WinAccordion.class, parent);
                            if(accordions != null) {
                                for(WinAccordion accordion : accordions) {
                                    if(accordion != Pointer) {
                                        accordion.setExpanded(false);
                                    }
                                }
                            }
                        }
                        catch(Exception ex) {
                        }
                    }
                }
            });
        }

        if(attrs != null) {
            TypedArray items = Pointer.getContext().obtainStyledAttributes(attrs, R.styleable.WinAppStyleable, 0, 0);
            try {
                /*----- Setting title -----*/
                String title = items.getString(R.styleable.WinAppStyleable_title);
                if(Pointer._vw_title != null) {
                    Pointer._vw_title.setText(title);
                }
                /*----- Setting Image Source -----*/
                Drawable src = items.getDrawable(R.styleable.WinAppStyleable_icon);
                Pointer._img_icon.setImageDrawable(src);
            } catch (Exception ex) {
            }
            items.recycle();
        }

        Pointer._onEventSelectedCallback = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg != null && msg.obj != null && msg.obj instanceof EventItem) {
                    EventItem item = (EventItem) msg.obj;
                    if(item.IsSelected) {
                        if(Pointer.getAllowMultiSelect() == false) {
                            try {
                                if (Pointer._lvw_items != null && Pointer._lvw_items.getAdapter() instanceof AccordionAdapter) {
                                    AccordionAdapter adapter = ((AccordionAdapter) Pointer._lvw_items.getAdapter());
                                    List<EventItem> events = adapter.getItems();
                                    if (events != null) {
                                        for (EventItem event : events) {
                                            if (event != item) {
                                                event.IsSelected = false;
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                Pointer.setExpanded(false);
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
                Pointer.setSelectedDesc();
                return true;
            }
        });
    }
    private void setSelectedDesc() {
        if(Pointer._vw_desc != null) {
            String desc = "";
            if(Pointer.isEditable()) {
                desc = Pointer._txt_remark.getText().toString();
            }
            else {
                String[] items = Pointer.getSelectedItems();
                if(items != null) {
                    desc = Common.join(items, ", ");
                }
            }
            Pointer._vw_desc.setText(desc);
            Pointer.setToggleButtonImage();
        }
    }
    private void setToggleButtonImage() {
        if(Pointer._img_toggle != null) {
            if(Pointer.isExpanded()) {
                Pointer._img_toggle.setImageDrawable(Common.getDrawable(getContext(), R.drawable.win_minus));
            }
            else {
                String desc = "";
                if (Pointer._vw_desc != null) {
                    desc = Pointer._vw_desc.getText().toString();
                }
                if (TextUtils.isEmpty(desc)) {
                    Pointer._img_toggle.setImageDrawable(Common.getDrawable(getContext(), R.drawable.win_add));
                } else {
                    Pointer._img_toggle.setImageDrawable(Common.getDrawable(getContext(), R.drawable.win_checked));
                }
            }
        }
    }

    public String[] getItems() {
        List<String> items = new ArrayList<>();
        try {
            if(Pointer._lvw_items != null && Pointer._lvw_items.getAdapter() instanceof AccordionAdapter) {
                List<EventItem> events = ((AccordionAdapter) Pointer._lvw_items.getAdapter()).getItems();
                if(events != null) {
                    for(EventItem event : events) {
                        items.add(event.Text);
                    }
                }
            }
        }
        catch(Exception ex) {
        }
        return items.toArray(new String[items.size()]);
    }
    public void setItems(String[] items) {
        if (Pointer._lvw_items != null) {
            List<EventItem> eventItems = new ArrayList<>();
            if (items != null) {
                for (String item : items) {
                    eventItems.add(new EventItem(item, item));
                }
            }
            Pointer._lvw_items.setAdapter(new AccordionAdapter(Pointer.getContext(), eventItems, Pointer._onEventSelectedCallback));
            int height = (eventItems.size() * Common.convertDpToPx(50, Pointer.getContext())) + 10;
            Pointer._lvw_items.setMinimumHeight(height);
            Pointer._lvw_items.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height));
        }
    }
    public String[] getSelectedItems() {
        List<String> items = new ArrayList<>();
        try {
            if(Pointer._lvw_items != null && Pointer._lvw_items.getAdapter() instanceof AccordionAdapter) {
                List<EventItem> events = ((AccordionAdapter) Pointer._lvw_items.getAdapter()).getItems();
                if(events != null) {
                    for(EventItem event : events) {
                        if(event.IsSelected == true) {
                            items.add(event.Text);
                        }
                    }
                }
            }
        }
        catch(Exception ex) {
        }
        return items.toArray(new String[items.size()]);
    }
    public void setSelectedItems(String[] items) {
        try {
            if(Pointer._lvw_items != null && Pointer._lvw_items.getAdapter() instanceof AccordionAdapter) {
                AccordionAdapter adapter = ((AccordionAdapter) Pointer._lvw_items.getAdapter());
                List<EventItem> events = adapter.getItems();
                if(events != null) {
                    for(EventItem event : events) {
                        event.IsSelected = false;
                        if(items != null) {
                            for (String item : items) {
                                if (item.compareToIgnoreCase(event.ID) == 0) {
                                    event.IsSelected = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            Pointer.setSelectedDesc();
        }
        catch(Exception ex) {
        }
    }
    public boolean isExpanded() {
        return Pointer._isExpanded;
    }
    public void setExpanded(boolean status) {
        Pointer._isExpanded = status;
        if(Pointer._vw_content != null) {
            Pointer._vw_content.setVisibility(Pointer._isExpanded ? View.VISIBLE : View.GONE);
        }
        Pointer.setSelectedDesc();
    }
    public void setEditable(boolean status) {
        Pointer._editable = status;
        if(Pointer._lvw_items != null) {
            Pointer._lvw_items.setVisibility(status ? View.GONE : View.VISIBLE);
        }
        if(Pointer._txt_remark != null) {
            Pointer._txt_remark.setVisibility(status ? View.VISIBLE : View.GONE);
        }
        Pointer.setSelectedDesc();
    }
    public boolean isEditable() {
        return Pointer._editable;
    }
    public void setRemark(String text) {
        if(Pointer._txt_remark != null) {
            Pointer._txt_remark.setText(text);
        }
        Pointer.setSelectedDesc();
    }
    public String getRemark() {
        if(Pointer._txt_remark != null) {
            return Pointer._txt_remark.getText().toString();
        }
        return "";
    }
    public void setAllowMultiSelect(boolean status) {
        Pointer._isMultiSelect = status;
    }
    public boolean getAllowMultiSelect() {
        return Pointer._isMultiSelect;
    }
}
