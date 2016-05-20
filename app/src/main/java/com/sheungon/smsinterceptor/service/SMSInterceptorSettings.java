package com.sheungon.smsinterceptor.service;

import android.support.annotation.NonNull;

import com.sheungon.smsinterceptor.util.PrivatePrefUtil;

/**
 * Settings for SMS interceptor
 * @author John
 */
public class SMSInterceptorSettings {

    private static final String KEY_SERVER_BASE_URL = "ServerBaseUrl";
    private static final String KEY_SERVER_API = "ServerAPI";


    @NonNull
    public static String getServerBaseUrl() {

        String baseUrl = PrivatePrefUtil.getString(KEY_SERVER_BASE_URL);

        return baseUrl == null ? "" : baseUrl;
    }

    public static void setServerBaseUrl(@NonNull String serverBaseUrl) {
        PrivatePrefUtil.set(KEY_SERVER_BASE_URL, serverBaseUrl);
    }

    @NonNull
    public static String getServerApi() {

        String api = PrivatePrefUtil.getString(KEY_SERVER_API);

        return api == null ? "" : api;
    }

    public static void setServerApi(@NonNull String api) {
        PrivatePrefUtil.set(KEY_SERVER_API, api);
    }
}
