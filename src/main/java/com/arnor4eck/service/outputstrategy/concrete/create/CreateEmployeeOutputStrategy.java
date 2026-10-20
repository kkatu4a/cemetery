package com.arnor4eck.service.outputstrategy.concrete.create;

import com.arnor4eck.model.Employee;
import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.concrete.create.CreateValueOutputStrategy;
import com.arnor4eck.util.enums.Role;
import com.arnor4eck.util.exceptions.CreateValueException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CreateEmployeeOutputStrategy extends CreateValueOutputStrategy<Employee> {
    private static final Pattern FULLNAME_PATTERN =
            Pattern.compile("^[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?"
                    + "(?:\\s+[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?){2}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^.*(?=.{9,})((?=.*[!@#$%^&*()\\-_=+{};:,<.>]){1})(?=.*\\d)((?=.*[a-z]){1})((?=.*[A-Z]){1}).*$");

    private static final Pattern LOGIN_PATTERN =
            Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]{1,18}[a-zA-Z0-9]$");

    public CreateEmployeeOutputStrategy(Repository<Employee> repository, Scanner scanner) {
        super(repository, scanner);
    }

    @Override
    public String act() {
        try {
            scanner.nextLine();
            String fullname = enterFullname();
            Role role = enterRole();
            String login = enterLogin();
            String password = enterPassword();

            var value = new Employee(0, fullname, role, login, hashPassword(password), true, LocalDateTime.now());

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

    private Role enterRole() {
        System.out.println("Выберите роль:");
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; ++i) {
            System.out.printf("\t%d. %s\n", i + 1, roles[i].name());
        }

        while (true) {
            try {
                System.out.print("Выберите действие: ");
                int choice = scanner.nextInt();

                if (choice >= 1 && choice <= roles.length) {
                    scanner.nextLine();
                    return roles[choice - 1];
                }

                System.out.println("Ошибка. Введите число от 1 до " + roles.length + ".");
            } catch (InputMismatchException e) {
                System.out.println("Некорректный ввод. Введите число из списка.");
            }
        }
    }

    private String enterLogin() {
        System.out.print("Введите логин: ");

        String input = scanner.nextLine().trim();

        if (!LOGIN_PATTERN.matcher(input).matches()) {
            throw new CreateValueException("Некорректно введен логин \n" +
                    "Логин может содержать только строчные и заглавные латинские буквы, цифры а также знак подчёркивания");
        }
        return input;
    }
    private String enterPassword(){
        System.out.print("Введите пароль: ");

        String input = scanner.nextLine().trim();

        if (!PASSWORD_PATTERN.matcher(input).matches()) {
            throw new CreateValueException("Некорректно введен пароль \n" +
                    "Требования к паролю:\n" +
                    "\n" +
                    "    только латинские (английские) буквы\n" +
                    "    не менее 9 символов\n" +
                    "    отсутствие пробелов\n" +
                    "    обязательное наличие хотя бы одной прописной(маленькой) и одной заглавной(большой) букв\n" +
                    "    обязательное наличие специальных символов");
        }
        return input;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм хеширования не найден", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
