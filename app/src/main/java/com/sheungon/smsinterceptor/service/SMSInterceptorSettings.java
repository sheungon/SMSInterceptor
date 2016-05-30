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
    private static final String KEY_ACCOUNT_NUMBER = "AccountNumber";
    private static final String KEY_BANK_CODE = "BankCode";


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

    @NonNull
    public static String getAccountNumber() {

        String accountNumber = PrivatePrefUtil.getString(KEY_ACCOUNT_NUMBER);

        return accountNumber == null ? "" : accountNumber;
    }

    public static void setAccountNumber(@NonNull String accountNumber) {
        PrivatePrefUtil.set(KEY_ACCOUNT_NUMBER, accountNumber);
    }

    @NonNull
    public static String getBankCode() {

        String bankCode = PrivatePrefUtil.getString(KEY_BANK_CODE);

        return bankCode == null ? "" : bankCode;
    }

    public static void setBankCode(@NonNull String bankCode) {
        PrivatePrefUtil.set(KEY_BANK_CODE, bankCode);
    }
}
