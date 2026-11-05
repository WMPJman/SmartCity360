package com.wmpj.smartcity360.core.complaint.chain;

public interface ComplaintHandler {

    ComplaintHandlingResult handle(ComplaintHandlingContext context);
}
