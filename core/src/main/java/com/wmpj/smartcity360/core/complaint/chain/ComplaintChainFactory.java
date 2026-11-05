package com.wmpj.smartcity360.core.complaint.chain;

public final class ComplaintChainFactory {

    public ComplaintHandler create() {
        LocalStaffHandler localStaff = new LocalStaffHandler();
        SupervisorHandler supervisor = new SupervisorHandler();
        CityManagerHandler cityManager = new CityManagerHandler();
        localStaff.setNext(supervisor);
        supervisor.setNext(cityManager);
        return localStaff;
    }
}
