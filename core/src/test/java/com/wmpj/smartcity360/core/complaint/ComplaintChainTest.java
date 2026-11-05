package com.wmpj.smartcity360.core.complaint;

import com.wmpj.smartcity360.core.complaint.chain.ComplaintChainFactory;
import com.wmpj.smartcity360.core.complaint.chain.ComplaintHandlingContext;
import com.wmpj.smartcity360.core.complaint.chain.ComplaintHandlingResult;
import com.wmpj.smartcity360.core.entity.ComplaintHandlerLevel;
import com.wmpj.smartcity360.core.entity.ComplaintSeverity;
import com.wmpj.smartcity360.core.entity.ComplaintStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintChainTest {

    @Test
    void routesSeverityThroughHandlersInOrder() {
        ComplaintHandlingResult localResult = handle(
                ComplaintSeverity.LOW,
                ComplaintHandlerLevel.LOCAL_STAFF,
                ComplaintStatus.SUBMITTED
        );
        ComplaintHandlingResult supervisorResult = handle(
                ComplaintSeverity.HIGH,
                ComplaintHandlerLevel.LOCAL_STAFF,
                ComplaintStatus.SUBMITTED
        );
        ComplaintHandlingResult managerResult = handle(
                ComplaintSeverity.CRITICAL,
                ComplaintHandlerLevel.LOCAL_STAFF,
                ComplaintStatus.SUBMITTED
        );

        assertEquals(ComplaintHandlerLevel.LOCAL_STAFF, localResult.responsibleLevel());
        assertEquals(ComplaintHandlerLevel.SUPERVISOR, supervisorResult.responsibleLevel());
        assertEquals(ComplaintHandlerLevel.CITY_MANAGER, managerResult.responsibleLevel());
        assertEquals(
                List.of(
                        ComplaintHandlerLevel.LOCAL_STAFF,
                        ComplaintHandlerLevel.SUPERVISOR,
                        ComplaintHandlerLevel.CITY_MANAGER
                ),
                managerResult.visitedLevels()
        );
    }

    @Test
    void preventsTerminalComplaintsFromBeingHandledAgain() {
        ComplaintHandlingResult result = handle(
                ComplaintSeverity.CRITICAL,
                ComplaintHandlerLevel.CITY_MANAGER,
                ComplaintStatus.RESOLVED
        );

        assertFalse(result.handled());
        assertEquals(3, result.visitedLevels().size());
    }

    @Test
    void validatesStateTransitionsAndFinalEscalation() {
        assertThrows(
                ComplaintWorkflowException.class,
                () -> ComplaintTransitionPolicy.requireRoutable(ComplaintStatus.IN_PROGRESS)
        );
        assertThrows(
                ComplaintWorkflowException.class,
                () -> ComplaintTransitionPolicy.requireEscalatable(ComplaintStatus.RESOLVED)
        );
        assertThrows(
                ComplaintWorkflowException.class,
                () -> ComplaintTransitionPolicy.requireResolvable(ComplaintStatus.SUBMITTED)
        );
        assertThrows(
                ComplaintWorkflowException.class,
                () -> ComplaintTransitionPolicy.nextLevel(ComplaintHandlerLevel.CITY_MANAGER)
        );
        assertTrue(handle(
                ComplaintSeverity.MEDIUM,
                ComplaintHandlerLevel.SUPERVISOR,
                ComplaintStatus.IN_PROGRESS
        ).handled());
    }

    private static ComplaintHandlingResult handle(
            ComplaintSeverity severity,
            ComplaintHandlerLevel level,
            ComplaintStatus status
    ) {
        return new ComplaintChainFactory().create().handle(
                new ComplaintHandlingContext(1L, severity, status, level)
        );
    }
}
