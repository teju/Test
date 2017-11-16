package com.win.winfertility.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.R;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.SendFeedbackArgs;
import com.win.winfertility.dataobjects.SendTextArgs;
import com.win.winfertility.dto.ApiResult;

public class RichTextInputManager {
    private Activity _activity;
    private RichTextInputManager Pointer;
    private boolean _isFeedback;

    private EditText _txt_text;
    private View _vw_richtext_input_dialog;
    private View linear_list;

    public RichTextInputManager(Activity activity) {
        this.Pointer = this;
        this._activity = activity;
    }

    public void init(View view) {
        /*----- Feedback Dialog Controls -----*/
        Pointer._txt_text = (EditText) Pointer._activity.findViewById(R.id.txt_text);
        linear_list = view;
        Pointer._vw_richtext_input_dialog = Pointer._activity.findViewById(R.id.vw_richtext_input_dialog);
        if(Pointer._vw_richtext_input_dialog != null) {
            Common.hideKeyboard(_activity);
            Pointer._vw_richtext_input_dialog.setVisibility(View.GONE);
            if(linear_list != null) {
                linear_list.setVisibility(View.VISIBLE);
            }
        }

        View btn_proceed = Pointer._activity.findViewById(R.id.btn_proceed);
        if(btn_proceed != null) {
            btn_proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.sendData();
                }
            });
        }

        View btn_feedback_close = Pointer._activity.findViewById(R.id.btn_feedback_close);
        if(btn_feedback_close != null) {
            btn_feedback_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Pointer._vw_richtext_input_dialog != null) {
                        Common.hideKeyboard(_activity);
                        Pointer._vw_richtext_input_dialog.setVisibility(View.GONE);
                        if(linear_list != null) {
                            linear_list.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }
    public void setTitle(String title) {
        TextView lbl_title = (TextView) this._vw_richtext_input_dialog.findViewById(R.id.lbl_title);
        if(lbl_title != null) {
            lbl_title.setText(title);
        }
    }
    public void showDialog(boolean isFeedback) {
        Pointer._isFeedback = isFeedback;
        if(Pointer._vw_richtext_input_dialog != null) {
            Pointer._vw_richtext_input_dialog.setVisibility(View.VISIBLE);
            if(linear_list != null) {
                linear_list.setVisibility(View.GONE);
            }
            if(Pointer._txt_text != null) {
                Pointer._txt_text.setText("");
                Pointer._txt_text.requestFocus();
            }
        }
    }

    private void sendData() {
        if(Pointer._txt_text != null) {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Pointer._vw_richtext_input_dialog != null) {
                        Pointer._vw_richtext_input_dialog.setVisibility(View.VISIBLE);
                        if(linear_list != null) {
                            linear_list.setVisibility(View.GONE);
                        }
                    }
                    Pointer._txt_text.requestFocus();
                }
            };

            if (Pointer._vw_richtext_input_dialog != null) {
                Common.hideKeyboard(_activity);
                Pointer._vw_richtext_input_dialog.setVisibility(View.GONE);
                if(linear_list != null) {
                    linear_list.setVisibility(View.VISIBLE);
                }
            }

            String text = Pointer._txt_text.getText().toString();
            System.out.println("FEEDBACKACTVIVIT?Y "+text.length());
            if(text.trim().length() == 0) {
                Notify.show(Pointer._activity, "Please enter your " + (Pointer._isFeedback ? "feedback" : " message text") + ".", onClickListener);
                return;
            }

            Loader.show(Pointer._activity);
            Object args = null;
            if(this._isFeedback) {
                SendFeedbackArgs feedbackArgs = new SendFeedbackArgs();
                feedbackArgs.EmailID = Shared.getString(Pointer._activity, Shared.KEY_EMAIL_ID);
                feedbackArgs.Feedback = text;
                args = feedbackArgs;
            }
            else {
                SendTextArgs textArgs = new SendTextArgs();
                textArgs.EmailID = Shared.getString(Pointer._activity, Shared.KEY_EMAIL_ID);
                textArgs.TextMessage = text;
                args = textArgs;
            }

            Common.invokeAPI(Pointer._activity, (Pointer._isFeedback ? ServiceMethods.SendFeedback : ServiceMethods.SendTextMessage), args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if(data != null && data.Result == 1) {
                                    if(Pointer._isFeedback) {
                                        Notify.show(Pointer._activity, "Thank you for providing us with your feedback. \nYour input is valuable for us to make improvements to this App. \nPlease check for updates periodically.");
                                    }
                                    else {
                                        Notify.show(Pointer._activity, "Thank you for submitting your text message. Due to the sensitive personal nature of this fertility App, and to ensure your privacy, a representative will contact you at the number you used to create this account within the next business day.");
                                    }
                                    return true;
                                }
                            }
                            if(TextUtils.isEmpty(response.Error) == false) {
                                error = response.Error;
                            }
                        }
                    }
                    catch (Exception ex) {
                    }
                    finally {
                        Loader.hide();
                    }
                    /*----- Handling Error -----*/
                    if(TextUtils.isEmpty(error)) {
                        error = AppMsg.MSG_FAILED;
                    }
                    Notify.show(Pointer._activity, error, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Pointer._vw_richtext_input_dialog != null) {
                                Pointer._vw_richtext_input_dialog.setVisibility(View.VISIBLE);
                                if(linear_list != null) {
                                    linear_list.setVisibility(View.GONE);
                                }
                            }
                            Pointer._txt_text.requestFocus();
                        }
                    });
                    return true;
                }
            }));
        }
    }
}
