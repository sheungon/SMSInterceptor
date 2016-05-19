package com.sheungon.smsinterceptor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * An easy way to access Android share preference privately.
 * Using the same shared preferences file.
 *
 * Created by johntsai on 28/7/15.
 * @author John
 */
@SuppressWarnings("unused")
public class PrivatePrefUtil {

    private static final String SHARED_PREF_FILE_KEY = "SMSInterceptorPrefs";
    private static Context _context;

    public static void init(@NonNull Context context) {
        _context = context;
    }

    /**
     * Async set. DB IO could work in background.
     * */
    public static void set(@NonNull String key,
                           @NonNull String value) {

        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Async set. DB IO could work in background.
     * */
    public static void set(@NonNull String key,
                           int value) {

        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Async set. DB IO could work in background.
     * */
    public static void set(@NonNull String key,
                           long value) {

        SharedPreferences.Editor editor = getEditor();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void set(@NonNull String key,
                           boolean value) {

        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Nullable
    public static String getString(@NonNull String key,
                                   @Nullable String defaultValue) {

        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString(key, defaultValue);
    }

    @Nullable
    public static String getString(@NonNull String key) {
        return getString(key, null);
    }

    public static int getInt(@NonNull String key,
                             int defaultValue) {

        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static long getLong(@NonNull String key,
                               int defaultValue) {

        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static boolean getBoolean(@NonNull String key,
                                     boolean defaultValue) {

        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void remove(@NonNull String key) {

        SharedPreferences.Editor editor = getEditor();
        editor.remove(key);
        editor.apply();
    }

    @NonNull
    private static SharedPreferences.Editor getEditor() {
        SharedPreferences sharedPref = _context.getSharedPreferences(SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);

        return sharedPref.edit();
    }

    @NonNull
    private static SharedPreferences getSharedPreferences() {

        return _context.getSharedPreferences(SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE);
    }
}
