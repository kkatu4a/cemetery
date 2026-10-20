package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Employee;
import com.katu4a.cladbise.util.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StubEmployeeService implements EmployeeService {

    private static final Pattern FULL_NAME_PATTERN =
            Pattern.compile("^[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?"
                    + "(?:\\s+[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?){2}$");
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]{1,18}[a-zA-Z0-9]$");
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+{};:,<.>";

    private final List<Employee> employees = new ArrayList<>();
    private int nextId = 7;

    public StubEmployeeService() {
        LocalDateTime now = LocalDateTime.now();
        String hash = PasswordHasher.sha256("Password1!");
        employees.add(new Employee(1, "Гой Гоевич Гоев", Role.ADMIN, "goy", hash, true, now));
        employees.add(new Employee(2, "Наш Слон Слонович", Role.MANAGER, "elephant", hash, true, now));
        employees.add(new Employee(3, "Полка Полка Полка", Role.MANAGER, "polka", hash, true, now));
        employees.add(new Employee(4, "Стул Стул Стулевич", Role.WORKER, "chair", hash, true, now));
        employees.add(new Employee(5, "Телефон Телефонов Телефонович", Role.WORKER, "phone", hash, false, now));
        employees.add(new Employee(6, "Кактотам Будейко Какойтотам", Role.MANAGER, "budeiko", hash, true, now));
    }

    @Override
    public List<Employee> findAll() {
        return List.copyOf(employees);
    }

    @Override
    public Employee create(Employee employee, String rawPassword) {
        validate(employee, rawPassword, true);
        Employee saved = new Employee(nextId++, employee.fullName(), employee.role(), employee.login(),
                PasswordHasher.sha256(rawPassword), employee.isActive(), LocalDateTime.now());
        employees.add(saved);
        return saved;
    }

    @Override
    public Employee update(Employee employee, String rawPassword) {
        validate(employee, rawPassword, false);
        for (int i = 0; i < employees.size(); i++) {
            Employee current = employees.get(i);
            if (current.id() == employee.id()) {
                String hash = isBlank(rawPassword) ? current.passwordHash() : PasswordHasher.sha256(rawPassword);
                Employee updated = new Employee(current.id(), employee.fullName(), employee.role(),
                        employee.login(), hash, employee.isActive(), current.createdAt());
                employees.set(i, updated);
                return updated;
            }
        }
        throw new ValidationException("Сотрудник не найден, возможно, он уже удалён");
    }

    @Override
    public void delete(int id) {
        boolean removed = employees.removeIf(employee -> employee.id() == id);
        if (!removed) {
            throw new ValidationException("Сотрудник не найден, возможно, он уже удалён");
        }
    }

    private void validate(Employee employee, String rawPassword, boolean passwordRequired) {
        String fullName = employee.fullName() == null ? "" : employee.fullName().trim();
        if (fullName.isEmpty()) {
            throw new ValidationException("fullName", "ФИО: поле обязательно для заполнения");
        }
        if (fullName.length() > 150) {
            throw new ValidationException("fullName", "ФИО: не длиннее 150 символов");
        }
        if (!FULL_NAME_PATTERN.matcher(fullName).matches()) {
            throw new ValidationException("fullName", "ФИО: введите три слова через пробел, только буквы");
        }

        if (employee.role() == null) {
            throw new ValidationException("role", "Роль: выберите роль из списка");
        }

        String login = employee.login() == null ? "" : employee.login().trim();
        if (login.isEmpty()) {
            throw new ValidationException("login", "Логин: поле обязательно для заполнения");
        }
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            throw new ValidationException("login",
                    "Логин: от 3 до 20 символов, латинские буквы, цифры, «_» и «-», начало и конец только буква или цифра");
        }
        boolean loginTaken = employees.stream().anyMatch(other ->
                other.id() != employee.id() && other.login().equalsIgnoreCase(login));
        if (loginTaken) {
            throw new ValidationException("login", "Логин: сотрудник с таким логином уже существует");
        }

        if (isBlank(rawPassword)) {
            if (passwordRequired) {
                throw new ValidationException("password", "Пароль: поле обязательно для заполнения");
            }
            return;
        }
        if (!isStrong(rawPassword)) {
            throw new ValidationException("password",
                    "Пароль: не менее 9 символов, латинские буквы в верхнем и нижнем регистре, цифра, спецсимвол, без пробелов");
        }
    }

    private boolean isStrong(String password) {
        if (password.length() < 9) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;
        for (char symbol : password.toCharArray()) {
            if (symbol >= 'A' && symbol <= 'Z') {
                upper = true;
            } else if (symbol >= 'a' && symbol <= 'z') {
                lower = true;
            } else if (symbol >= '0' && symbol <= '9') {
                digit = true;
            } else if (SPECIAL_CHARS.indexOf(symbol) >= 0) {
                special = true;
            } else {
                return false;
            }
        }
        return upper && lower && digit && special;
    }

    private boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }
}