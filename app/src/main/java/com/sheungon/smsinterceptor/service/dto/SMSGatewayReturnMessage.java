package com.sheungon.smsinterceptor.service.dto;

/**
 * Created by Han on 24/5/16.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class SMSGatewayReturnMessage {

    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("exception")
    @Expose
    private String exception;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("path")
    @Expose
    private String path;

    /**
     * @return The timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp The timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The error
     */
    public String getError() {
        return error;
    }

    /**
     * @param error The error
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * @return The exception
     */
    public String getException() {
        return exception;
    }

    /**
     * @param exception The exception
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path The path
     */
    public void setPath(String path) {
        this.path = path;
    }

}
