package com.sheungon.smsinterceptor.service;

import com.sheungon.smsinterceptor.service.dto.SMS;
import com.sheungon.smsinterceptor.service.dto.SMSGatewayReturnMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Han on 24/5/16.
 */
public interface SMSGatewayService {

    @POST
    Call<SMSGatewayReturnMessage> receiveSms(@Url String url, @Body SMS sms);
}
