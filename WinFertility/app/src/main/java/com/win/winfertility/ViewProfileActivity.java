package com.win.winfertility;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.BaseUserReqArgs;
import com.win.winfertility.dataobjects.ProfileDisplayData;
import com.win.winfertility.dataobjects.ProfileImageArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ViewProfileActivity extends WINFertilityActivity {
    public static ViewProfileActivity Instance;

    private ViewProfileActivity Pointer;
    private int REQUEST_CAMERA = 101;
    private int REQUEST_GALLERY = 102;

    private TextView _lbl_name;
    private TextView _lbl_dob;
    private TextView _lbl_goal;
    private TextView _lbl_bmi;
    private TextView _lbl_phone;
    private TextView _lbl_relationship;
    private TextView _lbl_company;
    private TextView _lbl_promocode;
    private View _vw_upload_photo;
    private TextView _lbl_upload_photo;
    private ImageView _img_profile;
    private String _selectedUploadMode = "";
    private boolean _permissionResult;
    private ProfileDisplayData _data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewProfileActivity.Instance = this;
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_view_profile);
        this.Pointer = this;
        this.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.loadProfileData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Common.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(Pointer._selectedUploadMode.equals("CAMERA"))
                        Pointer.takePhoto();
                    else if(Pointer._selectedUploadMode.equals("GALLERY"))
                        Pointer.chooseFromGallery();
                } else { }
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void init() {
        Pointer._img_profile = (ImageView) this.findViewById(R.id.img_profile);
        Pointer._lbl_name = (TextView) this.findViewById(R.id.lbl_name);
        Pointer._lbl_dob = (TextView) this.findViewById(R.id.lbl_dob);
        Pointer._lbl_goal = (TextView) this.findViewById(R.id.lbl_goal);
        Pointer._lbl_bmi = (TextView) this.findViewById(R.id.lbl_bmi);
        Pointer._lbl_phone = (TextView) this.findViewById(R.id.lbl_phone);
        Pointer._lbl_relationship = (TextView) this.findViewById(R.id.lbl_relationship);
        Pointer._lbl_company = (TextView) this.findViewById(R.id.lbl_company);
        Pointer._lbl_promocode = (TextView) this.findViewById(R.id.lbl_promocode);
        Pointer._vw_upload_photo = this.findViewById(R.id.vw_upload_photo);
        Pointer._lbl_upload_photo = (TextView) this.findViewById(R.id.lbl_upload_photo);

        if(Pointer._img_profile != null) {
            Pointer._img_profile.setImageBitmap(Common.getProfileImage(Pointer));
        }

        if(Pointer._vw_upload_photo != null) {
            Pointer._vw_upload_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.uploadProfilePhoto();
                }
            });
        }

        if(Pointer._lbl_upload_photo != null) {
            Pointer._lbl_upload_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.uploadProfilePhoto();
                }
            });
        }

        View btn_update_profile = this.findViewById(R.id.btn_update_profile);
        if(btn_update_profile != null) {
            btn_update_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Pointer, AccountOptionActivity.class);
                    intent.putExtra("DATA", new Gson().toJson(Pointer._data));
                    startActivity(intent);
                }
            });
        }
        View btn_log_out = this.findViewById(R.id.btn_log_out);
        if(btn_log_out != null) {
            btn_log_out.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Common.logOut(Pointer);
                }
            });
        }
    }
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bmp = null;
        if (data != null) {
            try {
                bmp = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Pointer.saveAndUploadImage(bmp);
    }
    private void onCaptureImageResult(Intent data) {
        Bitmap bmp = (Bitmap) data.getExtras().get("data");
        Pointer.saveAndUploadImage(bmp);
    }
    public String byteArrayToHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
    private void saveAndUploadImage(Bitmap actual) {
        final Bitmap bmp = Pointer.resizeBitmap(actual, 400);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] bytes = stream.toByteArray();

        ProfileImageArgs args = new ProfileImageArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        args.ProfilePhoto = Base64.encodeToString(bytes, Base64.DEFAULT);

        Loader.show(Pointer);
        Common.invokeAPI(Pointer, ServiceMethods.SaveProfilePhoto, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String error = "";
                try {
                    if(msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult result = (ApiResult) msg.obj;
                        if(result != null && TextUtils.isEmpty(result.Json) == false) {
                            ApiReqResult data = new Gson().fromJson(result.Json, ApiReqResult.class);
                            if(data != null && data.Result == 1) {
                                Pointer.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Common.setProfileImage(Pointer, bmp);
                                        if (Pointer._img_profile != null) {
                                            Pointer._img_profile.setImageBitmap(Common.getProfileImage(Pointer));
                                        }
                                    }
                                });
                                return true;
                            }
                        }
                    }
                }catch(Exception ex) {
                }
                finally {
                    Loader.hide();
                }
                if(TextUtils.isEmpty(error)) {
                    error = AppMsg.MSG_FAILED;
                }
                Notify.show(Pointer, error);
                return false;
            }
        }));
    }
    private void uploadProfilePhoto() {
        Notify.show(Pointer, "Upload Your Profile Photo.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pointer._permissionResult = Common.checkPermission(Pointer);
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    Pointer.chooseFromGallery();
                }
                else if(which == DialogInterface.BUTTON_NEGATIVE) {
                    Pointer.takePhoto();
                }
            }
        }, "Gallery", "Camera", true);
    }
    private void takePhoto() {
        Pointer._selectedUploadMode = "CAMERA";
        if(Pointer._permissionResult) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }
    public Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void chooseFromGallery() {
        Pointer._selectedUploadMode = "GALLERY";
        if(Pointer._permissionResult) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
        }
    }
    private void loadProfileData() {
        Loader.show(Pointer);
        BaseUserReqArgs args = new BaseUserReqArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        Common.invokeAPI(Pointer, ServiceMethods.GetProfileDisplayData, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String error = null;
                try {
                    if (msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult response = (ApiResult) msg.obj;
                        if (TextUtils.isEmpty(response.Json) == false) {
                            ProfileDisplayData data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ProfileDisplayData.class);
                            if (data != null && TextUtils.isEmpty(data.ProfileName) == false) {
                                Pointer._data = data;
                                Pointer._lbl_name.setText(Common.replaceNull(data.ProfileName));
                                Pointer._lbl_dob.setText(Common.replaceNull(data.DateOfBirth));
                                Pointer._lbl_goal.setText(Common.replaceNull(data.Goal));
                                Pointer._lbl_bmi.setText(Common.replaceNull(data.BMI));
                                Pointer._lbl_phone.setText(Common.replaceNull(data.PhoneNumber));
                                Pointer._lbl_relationship.setText(Common.replaceNull(data.RelationshipStatus));
                                Pointer._lbl_company.setText(Common.replaceNull(data.Company));
                                Pointer._lbl_promocode.setText(Common.replaceNull(data.Promocode));
                                return true;
                            }
                        }
                        if (TextUtils.isEmpty(response.Error) == false) {
                            error = response.Error;
                        }
                    }
                } catch (Exception ex) {
                } finally {
                    Loader.hide();
                }
                    /*----- Handling Error -----*/
                if (TextUtils.isEmpty(error)) {
                    error = AppMsg.MSG_FAILED;
                }
                Notify.show(Pointer, error, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Pointer.onBackPressed();
                    }
                });
                return true;
            }
        }));
    }
}
