package com.biker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.biker.Utils.Constants;
import com.biker.Utils.CustomToast;
import com.biker.Utils.IsNetworkConnection;
import com.biker.Utils.PrintClass;
import com.biker.Utils.post_async;

import org.json.JSONException;
import org.json.JSONObject;

public class ReachedDestination extends AppCompatActivity {

    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    private EditText otp;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_otp);
        rootView=findViewById(android.R.id.content);

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        otp=(EditText)findViewById(R.id.otp);
        rootView.setBackgroundResource(0);

    }

    public void submit(View view){
        if (IsNetworkConnection.checkNetworkConnection(ReachedDestination.this)) {
            if(prefrence.getString("isLoggedIn", "").equals("true")) {
                String url = Constants.SERVER_URL + "booking/booking-otp-verification";
                JSONObject params = new JSONObject();
                try {
                    params.put("user_id", prefrence.getString("user_id", ""));
                    params.put("access_token", prefrence.getString("access_token", ""));
                    params.put("booking_id", getIntent().getStringExtra("booking_id"));
                    params.put("otp", otp.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
                new post_async(ReachedDestination.this, "ReachedDestination").execute(url, params.toString());
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm Login");
                alertDialog.setMessage("You are not logged in !! Would Youlike to login ??");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        editor.putString("isLoggedIn","false");
                        editor.commit();
                        Intent i=new Intent(ReachedDestination.this,Login.class);
                        i.putExtra("reached_dest","true");
                        startActivity(i);
                        finish();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }
    public void ResponseOfReachedDestination(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfChangeStatus resultString ", " has data "
                    + jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                Intent i=new Intent(this,MainActivity.class);
                startActivity(i);
                finish();
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));

            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));
            }
        }
        catch (Exception e){
            PrintClass.printValue("ResponseOfChangeStatus Exception ", e.toString());
        }
    }
}
