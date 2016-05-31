package com.sheungon.smsinterceptor.service.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author John
 */
@SuppressWarnings("unused")

public class SMS {

    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("accountNumber")
    @Expose
    private String accountNumber;
    @SerializedName("bankCode")
    @Expose
    private String bankCode;

    public SMS(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    /**
     *
     * @return
     * The phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     *
     * @param phoneNumber
     * The phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public SMS withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     *
     * @return
     * The message
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param message
     * The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public SMS withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
}