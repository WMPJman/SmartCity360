package com.wmpj.smartcity360.web.dto;

import java.math.BigDecimal;
import java.util.Map;

public class PolicyEvaluationRequest {

    private Map<String, BigDecimal> numericValues;
    private Map<String, Boolean> booleanValues;

    public Map<String, BigDecimal> getNumericValues() {
        return numericValues;
    }

    public void setNumericValues(Map<String, BigDecimal> numericValues) {
        this.numericValues = numericValues;
    }

    public Map<String, Boolean> getBooleanValues() {
        return booleanValues;
    }

    public void setBooleanValues(Map<String, Boolean> booleanValues) {
        this.booleanValues = booleanValues;
    }
}
