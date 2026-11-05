package com.wmpj.smartcity360.web.dto;

public class StreetlightStatusRequest {

    private Boolean on;
    private Short brightness;
    private String operationalStatus;

    public Boolean getOn() {
        return on;
    }

    public void setOn(Boolean on) {
        this.on = on;
    }

    public Short getBrightness() {
        return brightness;
    }

    public void setBrightness(Short brightness) {
        this.brightness = brightness;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }
}
