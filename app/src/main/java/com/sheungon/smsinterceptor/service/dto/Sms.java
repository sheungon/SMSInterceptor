package com.sheungon.smsinterceptor.service.dto;

/**
 * @author John
 */
@SuppressWarnings("unused")
public class Sms {

    String phoneNumber;
    String message;


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
