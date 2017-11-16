package com.win.winfertility;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.win.winfertility.dataobjects.BaseUserReqArgs;
import com.win.winfertility.dataobjects.ProviderSearchItem;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.ContactManager;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.SelectBox;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProviderSearchActivity extends WINFertilityActivity {
    private ProviderSearchActivity Pointer;
    private JSONArray jsonElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_provider_search);
        this.Pointer = this;
        this.init();
        ContactManager.Init(this);
        this.loadProviderSearch();
    }

    private void init() {
        final String providerLink = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_PROVIDER_SEARCH_URL));
        View vw_fertility_logo = this.findViewById(R.id.vw_fertility_logo);
        if(vw_fertility_logo != null) {
            vw_fertility_logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(providerLink)) {
                        Notify.show(Pointer, "Please call us at: " + Shared.getString(Pointer, Shared.KEY_PHONE) + ", we can assist you with the selection of a provider within your network.");
                    }
                    else {
                        Pointer.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(providerLink)));
                                } catch (Exception ex) {
                                }
                            }
                        });
                        /*Notify.show(Pointer, "To take advantage of the WINFertility program and the special pricing discounts available; you must register (at no cost) with WINFertility.  \nPlease call us at: " +
                                Shared.getString(Pointer, Shared.KEY_PHONE) +
                                " to register. \nPlease note that if you receive benefits through your employer or health plan, we can assist with the selection of a provider within your network", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });*/
                    }
                }
            });
        }
    }
    private void loadProviderSearch() {

        final View pnl_win_fertility = Pointer.findViewById(R.id.pnl_win_fertility);
        if(pnl_win_fertility != null) {
            pnl_win_fertility.setVisibility(View.VISIBLE);
        }
        final View pnl_employer = Pointer.findViewById(R.id.pnl_employer);
        if(pnl_employer != null) {
            pnl_employer.setVisibility(View.GONE);
        }

        Loader.show(Pointer);
        BaseUserReqArgs args = new BaseUserReqArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        Common.invokeAPI(Pointer, ServiceMethods.LoadProviderSearch, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                    final ApiResult result = (ApiResult) msg.obj;
                    if(result != null && TextUtils.isEmpty(result.Json) == false) {
                        final List<ProviderSearchItem> items = new Gson().fromJson(result.Json, new TypeToken<List<ProviderSearchItem>>(){}.getType());
                        if(items != null && items.size() > 0) {
                            Pointer.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        jsonElements =new JSONArray(result.Json);

                                    }catch (Exception e){
                                        System.out.println("PROVIDERLOGIURL Exception "+ items);

                                    }
                                   /*// ImageView img_employer = (ImageView) Pointer.findViewById(R.id.img_employer);
                                    if (img_employer != null) {
                                        img_employer.setVisibility(View.GONE);
                                        Bitmap logo = Common.getEmployerLogo(Pointer);
                                        if (logo != null) {
                                            img_employer.setVisibility(View.VISIBLE);
                                            img_employer.setImageBitmap(Common.getEmployerLogo(Pointer));
                                        }
                                    }*/

                                    if(items == null || items.size() == 0) {
                                        pnl_win_fertility.setVisibility(View.VISIBLE);
                                        pnl_employer.setVisibility(View.GONE);
                                    }
                                    else {
                                        pnl_win_fertility.setVisibility(View.GONE);
                                        pnl_employer.setVisibility(View.VISIBLE);
                                    }

                                    final List<ProviderSearchItem> links = items;
                                    ListView lvw_links = (ListView) Pointer.findViewById(R.id.lvw_links);
                                    if(lvw_links != null) {
                                        lvw_links.setAdapter(new BaseAdapter() {
                                            @Override
                                            public int getCount() {
                                                return links.size();
                                            }
                                            @Override
                                            public ProviderSearchItem getItem(int position) {
                                                return links.get(position);
                                            }
                                            @Override
                                            public long getItemId(int position) {
                                                return position;
                                            }
                                            @Override
                                            public View getView(int position, View view, ViewGroup parent) {
                                                if (view == null) {
                                                    view = Pointer.getLayoutInflater().inflate(R.layout.view_provider_search_link_item, null);
                                                }
                                                if(view != null) {
                                                    ProviderSearchItem item = this.getItem(position);
                                                    try {
                                                        JSONObject jsonObject =jsonElements.getJSONObject(position);
                                                        ImageView vw_text = (ImageView) view.findViewById(R.id.vw_text);
                                                        try {
                                                            byte[] bytes = new byte[]{};
                                                            if (TextUtils.isEmpty(jsonObject.getString("Logo")) == false) {
                                                                bytes = Base64.decode(jsonObject.getString("Logo"), Base64.DEFAULT);
                                                            }
                                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                            vw_text.setImageBitmap(bmp);

                                                        } catch (Exception ex) {
                                                            System.out.println("PROVIDERLOGIURL Exception "+ ex.toString());
                                                        }
                                                   /* item.LinkName = "<u>" + item.LinkName + "</u>";
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                                        vw_text.setText(Html.fromHtml(item.LinkName,Html.FROM_HTML_MODE_LEGACY));
                                                    } else {
                                                        vw_text.setText(Html.fromHtml(item.LinkName));
                                                    }*/

                                                    vw_text.setTag(item.ProviderSearchLink);
                                                    vw_text.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if(v.getTag() != null && v.getTag() instanceof String) {
                                                                String link = v.getTag().toString();
                                                                try {
                                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                                                                }
                                                                catch(Exception ex) {
                                                                }
                                                            }
                                                        }
                                                    });
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                return view;
                                            }
                                        });
                                        SelectBox.setListViewHeightBasedOnItems(lvw_links, null);
                                    }
                                }
                            });
                        }
                    }
                }
                Loader.hide();
                return true;
            }
        }));
    }
}
