package com.arnor4eck.service.outputstrategy.concrete.create;

import com.arnor4eck.model.Customer;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.util.exceptions.CreateValueException;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CreateCustomerOutputStrategy extends CreateValueOutputStrategy<Customer> {

    private static final Pattern FULLNAME_PATTERN =
            Pattern.compile("^[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?"
                    + "(?:\\s+[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?){2}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+7\\d{10}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public CreateCustomerOutputStrategy(Repository<Customer> repository, Scanner scanner) {
        super(repository, scanner);
    }

    @Override
    public String act() {
        try {
            scanner.nextLine();
            String fullname = enterFullname();
            String phone = enterPhone();
            String email = enterEmail();

            var value = new Customer(0, fullname, phone, email, LocalDateTime.now());

            repository.save(value);
            return String.format("Сохранённое значение: %s", value);
        } catch (Exception e) {
            return String.format("Ошибка создания сущности: %s. Прогресс сброшен\n", e.getMessage());
        }
    }

    private String enterFullname() {
        System.out.print("Введите ФИО (3 слова через пробел, только буквы): ");

        String input = scanner.nextLine().trim();

        if (!FULLNAME_PATTERN.matcher(input).matches()) {
            throw new CreateValueException("Некорректно введено ФИО");
        }
        return input;
    }

    private String enterPhone() {
        System.out.print("Введите телефон в формате +7XXXXXXXXXX: ");

        String input = scanner.nextLine().trim();

        if (!PHONE_PATTERN.matcher(input).matches()) {
            throw new CreateValueException("Некорректно введен номер телефона");
        }
        return input;
    }

    private String enterEmail() {
        System.out.print("Введите email: ");

        String input = scanner.nextLine().trim();

        if (!EMAIL_PATTERN.matcher(input).matches()) {
            throw new CreateValueException("Некорректно введена электронная почта");
        }
        return input;
    }
}
