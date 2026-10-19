package com.arnor4eck;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class MenuProvider {

    private final Scanner scanner;
    private final String topic;
    private final List<String> units;

    public MenuProvider(
            String topic,
            Scanner scanner,
            List<String> units
    ) {
        this.scanner = scanner;
        this.topic = topic;
        this.units = units;
    }

    public int menu() {
        showMenu();
        return getNumber();
    }

    private void showMenu() {
        System.out.println(topic);
        for(int i = 0; i < units.size(); ++i) {
            System.out.printf("\t%d. %s\n", i + 1, units.get(i));
        }
        System.out.print("Выберите действие: ");
    }

    private int getNumber() {
        int num;

        while (true) {
            try{
                num = scanner.nextInt();
                break;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Некорректный ввод. Введите число из списка.");
            }
        }

        return num;
    }
}
