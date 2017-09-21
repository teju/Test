package com.example.teju.biker;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.teju.biker.Utils.Constants;
import com.example.teju.biker.Utils.CustomToast;
import com.example.teju.biker.Utils.IsNetworkConnection;
import com.example.teju.biker.Utils.PrintClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Teju on 19/09/2017.
 */
public class Login extends AppCompatActivity {

    private View rootView;
    private EditText phone,otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        rootView=findViewById(android.R.id.content);
        phone=(EditText)findViewById(R.id.phone);
    }

    @Override
    protected void onResume() {
        super.onResume();
        phone.setText("");
    }

    public void login(View v){
        String getPhone=phone.getText().toString();

        if(getPhone.length()==0){
            phone.setError("Phone Number cannot be empty");
        } else if(!getPhone.matches(Constants.regexStr) || getPhone.length()!=10) {
            phone.setError("Your Phone Number is Invalid.");
        } else {
            phone.setError(null);
            if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
          /*  //   String url = Constants.SERVER_URL + "action=ReferFriend";
            String url = SyncStateContract.Constants.SERVER_URL + "user/referFriend.htm";
            String json = "[{\"" + "User_Id" + "\":" + "\"" + prefrence.getString("User_Id", "") + "\"" + ",\""
                    + "Device_Type" + "\":" + "\"" +"android"+ "\"" + "}]";
            new post_async(UserRegister.this, "UserRegister").execute(url, json);*/
                final Dialog mBottomSheetDialog = new Dialog(this);
                mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mBottomSheetDialog.setContentView(R.layout.otp);
                mBottomSheetDialog.setCancelable(true);
                mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                mBottomSheetDialog.show();
                //mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
                Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
                otp = (EditText) mBottomSheetDialog.findViewById(R.id.otp);

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String getotp = otp.getText().toString();
                        if (getotp.length() == 0) {
                            otp.setError("Enter the otp");
                        } else {
                            otp.setError(null);
                            if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
                                Intent i = new Intent(Login.this, MainActivity.class);
                                startActivity(i);
                                mBottomSheetDialog.cancel();
                            } else {
                                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                        "No Internet Connection");
                            }
                        }
                    }
                });
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }


}
