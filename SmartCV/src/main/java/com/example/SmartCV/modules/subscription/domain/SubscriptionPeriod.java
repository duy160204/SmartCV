package com.example.SmartCV.modules.subscription.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
}
