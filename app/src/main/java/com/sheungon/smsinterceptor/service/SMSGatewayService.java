package com.sheungon.smsinterceptor.service;

import com.sheungon.smsinterceptor.service.dto.SMSGatewayReturnMessage;
import com.sheungon.smsinterceptor.service.dto.SMS2;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Han on 24/5/16.
 */
public interface SMSGatewayService {

    @POST("smsgateway/receiveSms")
    Call<SMSGatewayReturnMessage> receiveSms(@Body SMS2 sms);
}
