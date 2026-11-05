package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.energy.EnergyProcessingResult;
import com.wmpj.smartcity360.core.energy.EnergyStreamOperation;

public interface EnergyProcessingService {

    EnergyProcessingResult process(
            String meterCode,
            byte[] payload,
            EnergyStreamOperation operation
    );
}
