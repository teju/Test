package com.biker;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.biker.Utils.Constants;
import com.biker.Utils.CustomToast;
import com.biker.Utils.IsNetworkConnection;
import com.biker.Utils.PrintClass;
import com.biker.Utils.post_async;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Teju on 19/09/2017.
 */
public class UserRegister extends AppCompatActivity {

    private View rootView;
    private EditText name,phone,email;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    String type="";
    boolean isUpdate=false;
    private boolean agreed=false;
    private EditText referral_id;
    String getReferralIdValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        agreed=false;
        final Typeface typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");
        final Typeface italic = Typeface.createFromAsset(getAssets(), "fonts/italic.ttf");

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        isUpdate=false;
        rootView=findViewById(android.R.id.content);
        name=(EditText)findViewById(R.id.name);
        referral_id=(EditText)findViewById(R.id.referral_id);
        final Button click=(Button)findViewById(R.id.buttonclick);
        phone=(EditText)findViewById(R.id.phone);
        LinearLayout terms_conditions = (LinearLayout) findViewById(R.id.terms_conditions);
        final TextView terms_condi_text = (TextView) findViewById(R.id.terms);
        email=(EditText)findViewById(R.id.email);
        name.setTypeface(typeface_luci);
        phone.setTypeface(typeface_luci);
        email.setTypeface(typeface_luci);
        click.setTypeface(typeface_luci);
        referral_id.setTypeface(typeface_luci);

        type=getIntent().getStringExtra("type");
        PrintClass.printValue("UserRegisterPrint type ",type);
        final CheckBox agree_main = (CheckBox)findViewById(R.id.agree);

        if(type.equals("edit")) {
            getProfileInfo();
            click.setText("UPDATE");
            agree_main.setVisibility(View.GONE);
        } else {
            name.setText("");
            email.setText("");
            phone.setText("");
        }
        agree_main.setChecked(agreed);
        terms_condi_text.setTypeface(italic);
        terms_conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(UserRegister.this);
                dialog.setCancelable(false);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.spinner);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();
                if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {

                    RequestQueue queue = Volley.newRequestQueue(UserRegister.this);
                    String url = "http://chouguleeducation.in/biker/api/web/user/customer-terms-condition";

// Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    dialog.dismiss();
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.getString("status").equals("success")) {
                                            final Dialog mBottomSheetDialog = new Dialog(UserRegister.this);
                                            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            mBottomSheetDialog.setContentView(R.layout.activity_terms_conditions);
                                            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                                            mBottomSheetDialog.show();
                                            Button ok = (Button) mBottomSheetDialog.findViewById(R.id.ok);
                                            ok.setTypeface(typeface_luci);

                                            ok.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    mBottomSheetDialog.dismiss();
                                                    agree_main.setChecked(agreed);
                                                }
                                            });
                                            final CheckBox agree = (CheckBox) mBottomSheetDialog.findViewById(R.id.agree);
                                            agree.setTypeface(italic);
                                            agree.setChecked(agreed);
                                            agree.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if(agree.isChecked()){
                                                        agreed = true;
                                                    }else  {
                                                        agreed=false;
                                                    }
                                                }
                                            });

                                            HtmlTextView termCon = (HtmlTextView) mBottomSheetDialog.findViewById(R.id.terms_conditions);
                                            // Spanned result = Html.fromHtml(childText);
                                            // txtListChild.setText(result);
                                            termCon.setTypeface(typeface_luci);

                                            termCon.setHtml(jsonObject.getString("terms"), new HtmlHttpImageGetter(termCon));

                                            // Display the first 500 characters of the response string.
                                        } else {
                                            dialog.dismiss();
                                            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                                    "Something went wrong please try again later");
                                        }
                                    } catch (Exception e) {
                                        dialog.dismiss();
                                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                                "Something went wrong please try again later");
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
// Add the request to the RequestQueue.
                    queue.add(stringRequest);

                } else {
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "No Internet Connection");
                }
            }
        });
    }

    public void getProfileInfo(){
        if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
            String url = Constants.SERVER_URL + "profile/user-profile";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));

            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(UserRegister.this, "UserGetProfileInfo").execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void submit(View v){
        if(type.equals("edit")) {
            update();
        } else {
            register();
        }
    }

    public void register(){
        if(validate()) {
            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                //   String url = Constants.SERVER_URL + "action=ReferFriend";
                String url = Constants.SERVER_URL + "user/register";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
                try {
                    params.put("first_name",name.getText().toString() );
                    params.put("email", email.getText().toString());
                    params.put("mobile_no",phone.getText().toString());
                    params.put("user_type", "customer");
                    if(referral_id.getText().toString().trim().length() != 0) {
                        getReferralIdValue = referral_id.getText().toString();
                    }
                    params.put("referel_id", getReferralIdValue);
                    jsonBody.put("ApiSignupForm", params);
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                new post_async(UserRegister.this, "UserRegister").execute(url, jsonBody.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void update(){
        if(validate()) {
            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                //   String url = Constants.SERVER_URL + "action=ReferFriend";
                String url = Constants.SERVER_URL + "profile/user-update-profile";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
                try {
                    params.put("first_name",name.getText().toString() );
                    params.put("email", email.getText().toString());
                    params.put("mobile_no",phone.getText().toString());
                    jsonBody.put("UpdateProfileForm", params);
                    jsonBody.put("user_id", prefrence.getString("user_id", ""));
                    jsonBody.put("access_token", prefrence.getString("access_token", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                new post_async(UserRegister.this, "UserUpdate").execute(url, jsonBody.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void ResponseOfRegister(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
                editor.putString("user_id",  jsonObject.getString("user_id"));
                editor.commit();
                if(type.equals("edit")) {
                    isUpdate=true;
                    getProfileInfo();
                } else {
                    Intent i = new Intent(this, Login.class);
                    i.putExtra("reached_dest","false");
                    startActivity(i);
                    finish();
                }
            } else {
                JSONObject errorjsonObject = jsonObject.getJSONObject("errors");
                if(errorjsonObject.has("mobile_no")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("mobile_no");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0));
                    PrintClass.printValue("UserRegisterREsponse jsonObject errorjsonObject " +
                            "", " has data " + jsonArray.getString(0));
                    return;
                } else if(errorjsonObject.has("email")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("email");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0) );
                    return;
                } else if(errorjsonObject.has("first_name")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("first_name");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0) );
                    return;
                }
            }
        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public void ResponseOfUserInfo(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                JSONObject userInfo=jsonObject.getJSONObject("userDetails");
                name.setText(userInfo.getString("first_name"));
                email.setText(userInfo.getString("email"));
                phone.setText(userInfo.getString("mobile_no"));
                editor.putString("name", userInfo.getString("first_name"));
                editor.putString("email",userInfo.getString("email"));
                editor.commit();
                if(isUpdate){
                    Intent i = new Intent(UserRegister.this, MainActivity.class);
                    startActivity(i);
                    isUpdate=false;
                }
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public boolean validate(){

        String getName=name.getText().toString();
        String getEmail=email.getText().toString();
        String getPhone=phone.getText().toString();

        Pattern p = Pattern.compile(Constants.regEx);
        Matcher m = p.matcher(getEmail);

        PrintClass.printValue("PRINTINGHRVLUES ", "name : " + getName + " phone " + getPhone + " email " + getEmail);
        if(getName.length() == 0) {
            phone.setError(null);
            name.setError("First name cannot be empty");
            return false;
        } else if(getPhone.length()==0){
            name.setError(null);
            phone.setError("Phone Number cannot be empty");
            return false;
        } else if(!getPhone.matches(Constants.regexStr) || getPhone.length()!=10) {
            name.setError(null);
            phone.setError("Your Phone Number is Invalid.");
            return false;

        } else if(!type.equals("edit") && !agreed) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Please accept to terms & conditions");
            return false;
        }  else if(getEmail.length() > 0) {
            if(!m.find()) {
                name.setError(null);
                phone.setError(null);
                email.setError("Email Id Invalid");
                return false;
            } else {
                return true;
            }
        } else {
            name.setError(null);
            phone.setError(null);
            email.setError(null);
            return  true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        name.setText("");
        phone.setText("");
        email.setText("");
    }
}
