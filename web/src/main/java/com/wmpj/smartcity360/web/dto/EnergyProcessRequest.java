package com.wmpj.smartcity360.web.dto;

public class EnergyProcessRequest {

    private String meterCode;
    private String operation;
    private String payloadBase64;

    public String getMeterCode() {
        return meterCode;
    }

    public void setMeterCode(String meterCode) {
        this.meterCode = meterCode;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPayloadBase64() {
        return payloadBase64;
    }

    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }
}
