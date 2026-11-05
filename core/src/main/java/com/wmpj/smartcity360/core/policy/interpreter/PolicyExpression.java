package com.wmpj.smartcity360.core.policy.interpreter;

@FunctionalInterface
public interface PolicyExpression<T> {

    T evaluate(PolicyEvaluationContext context);
}
