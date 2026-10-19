package com.arnor4eck.service.outputstrategy.concrete.update;

import com.arnor4eck.model.Plot;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.concrete.update.UpdateValueOutputStrategy;
import com.arnor4eck.util.enums.PlotStatus;
import com.arnor4eck.util.exceptions.CreateValueException;

import java.util.Optional;
import java.util.Scanner;

public class UpdatePlotOutputStrategy extends UpdateValueOutputStrategy<Plot> {
    public UpdatePlotOutputStrategy(Repository<Plot> repository, Scanner scanner) {
        super(repository, scanner);
    }

    @Override
    protected Plot updateValue(Plot value) throws CreateValueException {
        return new Plot(
            value.id(),
            enterInt("id сектора").orElse(value.sectorId()),
            enterInt("линию").orElse(value.rowNumber()),
            enterInt("номер места").orElse(value.plotNumber()),
            enterPlotStatus().orElse(value.status()),
            enterPositiveFloat("длину в сантиметрах").orElse(value.lengthCm()),
            enterPositiveFloat("ширину в сантиметрах").orElse(value.widthCm()),
            enterCoords().orElse(value.coordinates())
        );
    }

    private Optional<String> enterCoords() {
        System.out.print("Введите координаты (или нажмите enter, если не хотите менять): ");
        String entered = scanner.nextLine();

        if(entered.isBlank())
            return Optional.empty();

        return Optional.of(entered);
    }

    private Optional<Integer> enterInt(String value) throws CreateValueException {
        System.out.printf("Введите %s (или нажмите enter, если не хотите менять): ", value);
        String entered = scanner.nextLine();

        if(entered.isBlank())
            return Optional.empty();

        try {
            int num = Integer.parseInt(entered);
            if (num <= 0) throw new CreateValueException("Введённое число должно быть положительным");
            return Optional.of(num);
        } catch (NumberFormatException e) {
            throw new CreateValueException("Некорректно введено число.");
        }
    }

    private Optional<Float> enterPositiveFloat(String param) throws CreateValueException {
        System.out.printf("Введите %s (или нажмите enter, если не хотите менять): ", param);

        String entered = scanner.nextLine();

        if(entered.isBlank())
            return Optional.empty();

        try {
            float num = Float.parseFloat(entered);

            if(num <= 0)
                throw new CreateValueException("Введённое число не может быть меньше или равно 0");

            return Optional.of(num);
        } catch (NumberFormatException e) {
            throw new CreateValueException("Некорректно введено число %s".formatted(param));
        }
    }

    private Optional<PlotStatus> enterPlotStatus() {
        System.out.print("Введите новый статус (или нажмите enter, если не хотите менять): ");

        String entered = scanner.nextLine();

        if(entered.isBlank())
            return Optional.empty();

        return Optional.of(PlotStatus.fromString(entered));
    }
}
