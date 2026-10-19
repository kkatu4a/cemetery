package com.arnor4eck.service.outputstrategy.concrete;

import com.arnor4eck.MenuProvider;
import com.arnor4eck.service.outputstrategy.DataExportStrategy;
import com.arnor4eck.service.outputstrategy.OutputStrategy;
import com.arnor4eck.util.OutputStrategyPair;

import java.util.List;
import java.util.Scanner;

public class MenuProviderOutputStrategy extends MenuProvider implements OutputStrategy {

    private final DataExportStrategy dataExportStrategy;

    public MenuProviderOutputStrategy(
            String topic,
            Scanner scanner,
            List<OutputStrategyPair<?>> pairs
    ) {
        super(topic, scanner, pairs.stream().map(OutputStrategyPair::unit).toList());
        this.dataExportStrategy = new DataExportStrategy(pairs.stream().map(OutputStrategyPair::strategy).toList());
    }

    @Override
    public String act() {
        int entered = menu();
        OutputStrategy strategy = dataExportStrategy.find(entered);

        return strategy.act();
    }
}
