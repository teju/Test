package com.win.winfertility;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.ContactManager;
import com.win.winfertility.utils.GraphManager;
import com.win.winfertility.utils.Shared;

public class ContactActivity extends WINFertilityActivity {
    private ContactActivity Pointer;

    private GraphManager _graphManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_contact);
        this.Pointer = this;
        this.init();
        ContactManager.Init(this);
    }

    private void init() {
        View.OnTouchListener menuOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setBackgroundColor(Color.parseColor("#3080A6DE"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.setBackgroundColor(Color.parseColor("#00000000"));
                }
                return false;
            }
        };
        View.OnClickListener menuOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch (id) {
                    case R.id.vw_resources:
                        Intent intent = new Intent(ContactActivity.this, BrowserActivity.class);
                        intent.putExtra("URL", Shared.KEY_FERTILITY_EDU_URL);
                        startActivity(intent);
                        break;
                    case R.id.vw_graph:
                        Pointer._graphManager.showDialog();
                        break;
                }
            }
        };

        View vw_resources = Pointer.findViewById(R.id.vw_resources);
        View vw_graph = Pointer.findViewById(R.id.vw_graph);

        if(vw_resources != null) {
            vw_resources.setOnTouchListener(menuOnTouchListener);
            vw_resources.setOnClickListener(menuOnClickListener);
        }
        if(vw_graph != null) {
            vw_graph.setOnTouchListener(menuOnTouchListener);
            vw_graph.setOnClickListener(menuOnClickListener);
        }

        /*----- Graph Manager Functionality -----*/
        Pointer._graphManager = new GraphManager(this);
        Pointer._graphManager.init(null);
    }
}
