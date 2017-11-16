package com.win.winfertility;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.Shared;

public class BrowserActivity extends WINFertilityActivity {
    private BrowserActivity Pointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_browser);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        String type = "", url = "", title = "";
        /*----- Finding intent -----*/
        Intent intent = Pointer.getIntent();
        if(intent.hasExtra("URL")) {
            type = intent.getStringExtra("URL");
        }
        /*----- Finding url details from intent -----*/
        if(type.compareToIgnoreCase(Shared.KEY_BENEFITS_OVERVIEW_URL) == 0) {
            url = Shared.getString(Pointer, Shared.KEY_BENEFITS_OVERVIEW_URL);
            title = "BENEFITS OVERVIEW";
        }
        else if(type.compareToIgnoreCase(Shared.KEY_PROVIDER_SEARCH_URL) == 0) {
            url = Shared.getString(Pointer, Shared.KEY_PROVIDER_SEARCH_URL);
            title = "PROVIDER SEARCH";
        }
        else if(type.compareToIgnoreCase(Shared.KEY_LEGAL_URL) == 0) {
            url = Shared.getString(Pointer, Shared.KEY_LEGAL_URL);
            title = "LEGAL";
        }
        else {
            url = Shared.getString(Pointer, Shared.KEY_FERTILITY_EDU_URL);
            title = "FERTILITY EDUCATION";
        }

        TextView lbl_text = (TextView) Pointer.findViewById(R.id.lbl_text);
        if(lbl_text != null) {
            lbl_text.setText(title);
        }

        if(TextUtils.isEmpty(url)) {
            Notify.show(Pointer, "Sorry, this page is currently unavailable, please try later.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer.onBackPressed();
                }
            });
        }
        else {
            Loader.show(Pointer);
            WebView web_browser = (WebView) this.findViewById(R.id.web_browser);
            if (web_browser != null) {
                web_browser.setWebViewClient(new WebViewClient(){
                    public void onPageFinished(WebView view, String url) {
                        Loader.hide();
                    }
                });
                web_browser.getSettings().setJavaScriptEnabled(true);
                web_browser.loadUrl(url);
            }
        }
    }

    @Override
    public void onBackPressed() {
        WebView web_browser = (WebView) this.findViewById(R.id.web_browser);
        if (web_browser != null) {
            if(web_browser.canGoBack()) {
                web_browser.goBack();
                return;
            }
        }
        super.onBackPressed();
    }
}
