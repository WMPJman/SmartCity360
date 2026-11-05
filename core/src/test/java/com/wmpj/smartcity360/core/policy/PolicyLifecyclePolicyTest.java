package com.wmpj.smartcity360.core.policy;

import com.wmpj.smartcity360.core.entity.CityPolicyStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyLifecyclePolicyTest {

    @Test
    void permitsOnlyDraftActivationAndActiveDeactivation() {
        assertDoesNotThrow(
                () -> PolicyLifecyclePolicy.requireActivatable(CityPolicyStatus.DRAFT)
        );
        assertDoesNotThrow(
                () -> PolicyLifecyclePolicy.requireDeactivatable(CityPolicyStatus.ACTIVE)
        );
        assertThrows(
                PolicyLifecycleException.class,
                () -> PolicyLifecyclePolicy.requireActivatable(CityPolicyStatus.ACTIVE)
        );
        assertThrows(
                PolicyLifecycleException.class,
                () -> PolicyLifecyclePolicy.requireDeactivatable(CityPolicyStatus.INACTIVE)
        );
    }
}
