package com.wmpj.smartcity360.core.policy;

import com.wmpj.smartcity360.core.entity.CityPolicyStatus;

public final class PolicyLifecyclePolicy {

    private PolicyLifecyclePolicy() {
    }

    public static void requireActivatable(CityPolicyStatus status) {
        if (status != CityPolicyStatus.DRAFT) {
            throw new PolicyLifecycleException("Only draft policies can be activated");
        }
    }

    public static void requireDeactivatable(CityPolicyStatus status) {
        if (status != CityPolicyStatus.ACTIVE) {
            throw new PolicyLifecycleException("Only active policies can be deactivated");
        }
    }
}
