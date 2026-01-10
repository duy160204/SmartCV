package com.example.SmartCV.modules.subscription.domain;

public enum PlanType {
    FREE(1),
    PRO(2),
    PREMIUM(3);

    private final int level;

    PlanType(int level) {
        this.level = level;
    }

    public boolean isAtLeast(PlanType other) {
        return this.level >= other.level;
    }
}
