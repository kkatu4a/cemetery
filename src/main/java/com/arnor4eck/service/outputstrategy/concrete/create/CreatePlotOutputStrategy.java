package com.arnor4eck.service.outputstrategy.concrete.create;

import com.arnor4eck.model.Plot;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.util.enums.PlotStatus;

import java.util.Scanner;

public class CreatePlotOutputStrategy extends CreateValueOutputStrategy<Plot>{
    public CreatePlotOutputStrategy(Repository<Plot> repository, Scanner scanner) {
        super(repository, scanner);
    }

    @Override
    public String act() {
        try {
            int sectorId = enterPositiveInteger("id сектора");
            int rowNumber = enterPositiveInteger("номер ряда");
            int plotNumber = enterPositiveInteger("номер места");
            PlotStatus plotStatus = enterPlotStatus();
            float length = enterPositiveFloat("длину (сантиметры)");
            float width = enterPositiveFloat("ширину (сантиметры)");
            String coords = enterCoords();

            var value = new Plot(
                0, sectorId, rowNumber, plotNumber, plotStatus, length, width, coords
            );

            repository.save(value);
            return String.format("Сохранённое значение: %s", value);
        } catch (Exception e) {
            return String.format("Ошибка создания сущности: %s. Прогресс сброшен\n", e.getMessage());
        }
    }

    private PlotStatus enterPlotStatus() {
        System.out.println("Введите одно из значений: ");
        for (var plotStatus : PlotStatus.values()) {
            System.out.printf("%s (%s)\n", plotStatus.name(), plotStatus.getValue());
        }

        return PlotStatus.fromString(scanner.nextLine());
    }

    private String enterCoords() {
        System.out.println("Введите координаты: ");
        return scanner.nextLine();
    }
}
