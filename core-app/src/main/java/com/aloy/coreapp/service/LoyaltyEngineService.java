package com.aloy.coreapp.service;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface LoyaltyEngineService {

    BigInteger getPoints(BigDecimal orderAmount);

    void checkIfUserEligibleForBadge(String userId);
}
