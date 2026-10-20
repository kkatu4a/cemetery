package com.arnor4eck.util;

import com.arnor4eck.service.outputstrategy.OutputStrategy;

public record OutputStrategyPair<T extends OutputStrategy>(String unit, T strategy) {
    public static <R extends OutputStrategy> OutputStrategyPair<R> of(String unit, R strategy) {
        return new OutputStrategyPair<>(unit, strategy);
    }
}
