package com.win.winfertility.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.win.winfertility.R;
import com.win.winfertility.dto.SelectDialogItem;
import com.win.winfertility.tools.SelectDialogAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectBox {
    private static Dialog SelectDialog;
    private static EditText txt_reminder_type;
    String reminder_type="";
    public static boolean isShowing() {
        return (SelectDialog == null ? false : SelectDialog.isShowing());
    }
    public static void show(final Context context, final String title, final List<SelectDialogItem> items, final Handler callback) {
        SelectBox.show(context, title, items, callback, false);
    }
    public static void show(final Context context, final String title, final List<SelectDialogItem> items,
                            final Handler callback, final boolean isMultiSelect) {
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(SelectBox.SelectDialog != null) {
                    SelectBox.SelectDialog.dismiss();
                    SelectBox.SelectDialog = null;
                }

                SelectBox.SelectDialog = new Dialog(context);
                SelectBox.SelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                SelectBox.SelectDialog.setCancelable(true);
                SelectBox.SelectDialog.setContentView(R.layout.view_select_dialog);

                TextView vw_title = (TextView) SelectBox.SelectDialog.findViewById(R.id.vw_title);
                if(vw_title != null) {
                    vw_title.setText(title);
                }
                ListView lvw_items = (ListView) SelectBox.SelectDialog.findViewById(R.id.lvw_items);
                txt_reminder_type = (EditText) SelectBox.SelectDialog.findViewById(R.id.txt_reminder_type);
                System.out.println("SelectDialogItem123456jdjdffd : "+SelectDialogItem.desc+" "+items.get(0).ID);
                if(SelectDialogItem.desc.length() !=0 ) {
                    if(items.get(0).ID.startsWith("rem")) {
                        txt_reminder_type.setVisibility(View.VISIBLE);
                        txt_reminder_type.setText(SelectDialogItem.desc);
                    }
                }
                if(lvw_items != null) {
                    lvw_items.setAdapter(new SelectDialogAdapter(context, items, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(final Message msg) {
                            final SelectDialogItem item = (SelectDialogItem) msg.obj;
                            System.out.println("SelectDialogItem123456jdjdffd : inside "+item.ID+" "+isMultiSelect);
                            if(isMultiSelect == false) {
                                txt_reminder_type.setVisibility(View.GONE);
                                SelectBox.hide();
                                if (callback != null) {
                                    System.out.println("reminder_type12345 callback "+item.desc);
                                    callback.sendMessage(callback.obtainMessage(0, msg.obj));
                                }
                            } else {
                                System.out.println("reminder_type12345 ismultiselect23232 " + item.ID);
                                if(item.ID.equals("remT6")) {
                                    txt_reminder_type.setVisibility(View.VISIBLE);
                                } else {
                                    txt_reminder_type.setVisibility(View.GONE);
                                }
                            }
                            return true;
                        }
                    }), isMultiSelect));
                    SelectBox.setListViewHeightBasedOnItems(lvw_items, null);
                }

                View vw_panel = SelectBox.SelectDialog.findViewById(R.id.vw_panel);
                if(vw_panel != null) {
                    vw_panel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SelectBox.hide();
                        }
                    });
                }

                View vw_buttons = SelectBox.SelectDialog.findViewById(R.id.vw_buttons);
                if(vw_buttons != null) {
                    vw_buttons.setVisibility(isMultiSelect ? View.VISIBLE : View.GONE);
                    if(isMultiSelect) {
                        View btn_ok = SelectBox.SelectDialog.findViewById(R.id.btn_ok);
                        if(btn_ok != null) {
                            btn_ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    List<SelectDialogItem> selected = new ArrayList<>();

                                        for (SelectDialogItem item : items) {
                                            if (item.IsSelected) {
                                                selected.add(item);
                                            }
                                        }

                                        if(selected.size() !=0 ) {
                                            if (selected.get(0).ID.equals("remT6")) {
                                                if (txt_reminder_type.getVisibility() == View.GONE) {
                                                    txt_reminder_type.setVisibility(View.VISIBLE);
                                                }

                                                if (TextUtils.isEmpty(txt_reminder_type.getText().toString())) {
                                                    Notify.show(context, "Please enter reminder type description.", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            txt_reminder_type.requestFocus();
                                                        }
                                                    });
                                                } else {
                                                    SelectDialogItem.desc=txt_reminder_type.getText().toString();
                                                    callback.sendMessage(callback.obtainMessage(0, selected));
                                                    SelectBox.hide();
                                                }
                                            } else {
                                                txt_reminder_type.setText("");
                                                SelectDialogItem.desc = "";
                                                callback.sendMessage(callback.obtainMessage(0, selected));
                                                SelectBox.hide();
                                            }

                                        } else {
                                            Notify.show(context, "Please select the reminder type.", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    txt_reminder_type.requestFocus();
                                                }
                                            });
                                        }
                                }
                            });
                        }
                        View btn_cancel = SelectBox.SelectDialog.findViewById(R.id.btn_cancel);
                        if(btn_cancel != null) {
                            btn_cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    SelectBox.hide();
                                }
                            });
                        }
                    }
                }

                SelectBox.SelectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                SelectBox.SelectDialog.show();
                SelectBox.SelectDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
    }
    public static void hide() {


        if(SelectBox.SelectDialog != null) {
            new Handler(SelectBox.SelectDialog.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    SelectBox.SelectDialog.dismiss();
                    SelectBox.SelectDialog = null;
                }
            });
        }
    }
    public static void config(final TextView opener, final List<SelectDialogItem> items, final String title, final boolean isMultiSelect) {
        SelectBox.config(opener, items, title, isMultiSelect, null);
    }
    public static void config(final TextView opener, final List<SelectDialogItem> items, final String title) {
        SelectBox.config(opener, items, title, false, null);
    }
    public static void config(final TextView opener, final List<SelectDialogItem> items, final String title, final boolean isMultiSelect, final Handler callback) {
        if(opener != null) {
            opener.setFocusable(true);
            opener.setClickable(true);
            opener.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<SelectDialogItem> selectedItems = new ArrayList<>();
                    if(opener.getTag() != null) {
                        if(opener.getTag() instanceof SelectDialogItem) {
                            System.out.println("SelectDialogItem123456 tag  instanceof SelectDialogItem :"+opener.getTag());
                            selectedItems.add((SelectDialogItem) opener.getTag());
                        }
                        else if(opener.getTag() instanceof List) {
                            System.out.println("SelectDialogItem123456 tag  instanceof List :"+opener.getTag());
                            selectedItems = (List<SelectDialogItem>) opener.getTag();
                        }
                    }

                    for(SelectDialogItem item : items) {
                        item.IsSelected = false;
                        for(SelectDialogItem selected : selectedItems) {
                            System.out.println("SelectDialogItem123456 selected.ID :"+selected.ID +" item.ID "+item.ID);

                            if (TextUtils.isEmpty(selected.ID) == false && selected.ID.trim().compareToIgnoreCase(item.ID.trim()) == 0) {
                                System.out.println("SelectDialogItem123456 selected.ID :"+selected.desc);
                                if(!selected.desc.equals("Other")) {
                                    item.IsSelected = true;
                                }
                                break;
                            }
                        }
                    }

                    SelectBox.show(opener.getContext(), title, items, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            System.out.println("SelectDialogItem123456 msg.obj .ID :"+msg.obj);

                            if(msg.obj != null) {
                                //System.out.println("SelectDialogItem123456 msg.obj .ID :"+msg.obj);
                                opener.setTag(msg.obj);
                                System.out.println("SelectDialogItem123456 setText :"+SelectBox.getSelectBoxText(opener, "\n"));
                                opener.setText(SelectBox.getSelectBoxText(opener, "\n"));

                            }
                            if(callback != null) {
                                callback.sendMessage(callback.obtainMessage(0, msg.obj));
                            }
                            return true;
                        }
                    }), isMultiSelect);
                }
            });
        }
    }
    public static SelectDialogItem getData(TextView opener) {
        SelectDialogItem selected = null;
        if(opener.getTag() != null && opener.getTag() instanceof SelectDialogItem) {
            selected = (SelectDialogItem) opener.getTag();
        }
        else {
            selected = new SelectDialogItem();
        }
        return selected;
    }
    public static void setData(TextView opener, SelectDialogItem item) {
        opener.setTag(item);
        System.out.println("reminder_type12345 setData "+item.ID);

        opener.setText(item != null && TextUtils.isEmpty(item.ID) == false ? item.Text : "");
    }
    public static List<SelectDialogItem> getDataList(TextView opener) {
        List<SelectDialogItem> selected = null;
        if(opener.getTag() != null && opener.getTag() instanceof List) {
            selected = (List<SelectDialogItem>) opener.getTag();
        }
        else {
            selected = new ArrayList<>();
        }
        return selected;
    }
    public static void setDataList(TextView opener, String[] values) {
        List<SelectDialogItem> items = new ArrayList<>();
        for(String value : values) {
            items.add(new SelectDialogItem(value));
        }
        opener.setTag(items);
        opener.setText(Common.join(values, ", "));
    }
    public static String getSelectBoxText(TextView opener, String splitter) {
        String text = "";
        try {
            Object tag = opener.getTag();
            if(tag instanceof SelectDialogItem) {
                SelectDialogItem item = (SelectDialogItem) tag;
                text = item.Text;
            }
            else if(tag instanceof List) {
                List<SelectDialogItem> items = (List<SelectDialogItem>) tag;
                for(SelectDialogItem item : items) {
                    if(TextUtils.isEmpty(text) == false) {
                        text += splitter;
                    }
                    text += item.Text;
                }
            }
        }
        catch(Exception ex) {
        }
        return text;
    }
    public static void setListViewHeightBasedOnItems(ListView listView, ViewGroup container) {
        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();

        if (adapter != null) {
            int numberOfItems = adapter.getCount();
            /*----- Get total height of all items. -----*/
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = adapter.getView(itemPos, null, listView);
                item.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                item.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                item.layout(0, 0, item.getMeasuredWidth(), item.getMeasuredHeight());
                totalItemsHeight += item.getMeasuredHeight();
            }
            /*----- Get total height of all item dividers. -----*/
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);
            /*----- Set list height. -----*/
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
            /*----- redraw the container layout. -----*/
            if(container != null) {
                container.requestLayout();
            }
        }
    }
}
