package com.win.winfertility.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Shared {
    public static final String ACTION_FCM_INDIRECT_MSG = "ACTION_FCM_INDIRECT_MSG_WIN_FERTILITY";
    public static final String ACTION_FCM_INDIRECT_MSG_2 = "ACTION_FCM_INDIRECT_MSG_WIN_FERTILITY_2";
    public static final String ACTION_FCM_DIRECT_MSG = "ACTION_FCM_DIRECT_MSG_WIN_FERTILITY";
    public static final String ACTION_FCM_DIRECT_MSG_2 = "ACTION_FCM_DIRECT_MSG_WIN_FERTILITY_2";
    public static final String ACTION_FCM_TOKEN_ID = "ACTION_FCM_TOKEN_ID";

    public static final String EXTRA_GOAL = "EXTRA_GOAL";
    public static final String EXTRA_PARENT_CLASS = "EXTRA_PARENT_CLASS";
    public static final String EXTRA_IS_EDIT = "IS_EDIT";
    public static final String EXTRA_TOKEN_ID = "EXTRA_TOKEN_ID";
    public static final String EXTRA_NOTIFICATION_DATE = "EXTRA_NOTIFICATION_DATE";

    public static final String KEY_PREGNANCY_NOTIFICATION_DATE = "KEY_PREGNANCY_NOTIFICATION_DATE";
    public static final String KEY_NOTIFICATION_ENABLED = "KEY_NOTIFICATION_ENABLED";
    public static final String KEY_CREATED_EMAIL_ID = "KEY_CREATED_EMAIL_ID";
    public static final String KEY_PROFILE_INFO_SAVED = "KEY_PROFILE_INFO_SAVED";
    public static final String KEY_PROF_PAGE_1_INFO = "KEY_PROF_1_INFO";
    public static final String KEY_PROF_PAGE_2_INFO = "KEY_PROF_2_INFO";
    public static final String KEY_MENSTRUAL_INFO_SAVED = "KEY_MENSTRUAL_INFO_SAVED";
    public static final String KEY_MENSTRUAL_INFO = "KEY_MENSTRUAL_INFO";
    public static final String KEY_EMAIL_ID = "KEY_EMAIL_ID";
    public static final String KEY_PROFILE_NAME = "KEY_PROFILE_NAME";
    public static final String KEY_FERTILITY_EDU_URL = "KEY_FERTILITY_EDU_URL";
    public static final String KEY_PROVIDER_SEARCH_URL = "KEY_PROVIDER_SEARCH_URL";
    public static final String KEY_BENEFITS_OVERVIEW_URL = "KEY_BENEFITS_OVERVIEW_URL";
    public static final String KEY_LEGAL_URL = "KEY_LEGAL_URL";
    public static final String KEY_PHONE = "KEY_PHONE";
    public static final String KEY_ENROLLED = "KEY_ENROLLED";
    public static final String KEY_TOKEN_ID = "KEY_TOKEN_ID";
    public static final String KEY_PASSWORD = "KEY_PASSWORD";

    public static final String FCM_PAYLOAD_DATA_KEY = "ReminderDate";
    public static final String FCM_PAYLOAD_DATA_KEY_body = "NotificationBody";

    private static SharedPreferences.Editor getEditor(Context context) {
        return Shared.getSharedPref(context).edit();
    }
    private static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences("WIN_FERTILITY_DATA", Context.MODE_PRIVATE);
    }
    public static void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = Shared.getEditor(context);
        editor.putString(key, value);
        editor.apply();
    }
    public static String getString(Context context, String key) {
        try {
            return Shared.getSharedPref(context).getString(key, "");
        }
        catch(Exception ex) {
        }
        return "";
    }
    public static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = Shared.getEditor(context);
        editor.putInt(key, value);
        editor.apply();
    }
    public static int getInt(Context context, String key) {
        try {
            return Shared.getSharedPref(context).getInt(key, 0);
        }
        catch(Exception ex) {
        }
        return 0;
    }
    public static void clear(Context context) {
        SharedPreferences.Editor editor = Shared.getEditor(context);
        editor.clear();
        editor.apply();
    }
}
