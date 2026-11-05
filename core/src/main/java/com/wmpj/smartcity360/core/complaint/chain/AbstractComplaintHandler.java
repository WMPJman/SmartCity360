package com.wmpj.smartcity360.core.complaint.chain;

import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractComplaintHandler implements ComplaintHandler {

    private final ComplaintHandlerLevel handlerLevel;
    private ComplaintHandler nextHandler;

    protected AbstractComplaintHandler(ComplaintHandlerLevel handlerLevel) {
        this.handlerLevel = handlerLevel;
    }

    public AbstractComplaintHandler setNext(ComplaintHandler nextHandler) {
        this.nextHandler = nextHandler;
        return this;
    }

    @Override
    public ComplaintHandlingResult handle(ComplaintHandlingContext context) {
        return route(context, new ArrayList<>(), new HashSet<>());
    }

    private ComplaintHandlingResult route(
            ComplaintHandlingContext context,
            List<ComplaintHandlerLevel> visitedLevels,
            Set<ComplaintHandlerLevel> visitedSet
    ) {
        if (!visitedSet.add(handlerLevel)) {
            return new ComplaintHandlingResult(false, null, visitedLevels);
        }

        visitedLevels.add(handlerLevel);

        if (canHandle(context)) {
            return new ComplaintHandlingResult(true, handlerLevel, visitedLevels);
        }

        if (nextHandler instanceof AbstractComplaintHandler abstractNext) {
            return abstractNext.route(context, visitedLevels, visitedSet);
        }

        return new ComplaintHandlingResult(false, null, visitedLevels);
    }

    protected abstract boolean canHandle(ComplaintHandlingContext context);
}
