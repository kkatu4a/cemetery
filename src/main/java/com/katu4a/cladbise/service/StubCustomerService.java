package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Customer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StubCustomerService implements CustomerService {

    private static final Pattern FULL_NAME_PATTERN =
            Pattern.compile("^[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?"
                    + "(?:\\s+[A-Za-zА-Яа-яЁё]+(?:-[A-Za-zА-Яа-яЁё]+)?){2}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final List<Customer> customers = new ArrayList<>();
    private int nextId = 8;

    public StubCustomerService() {
        LocalDateTime now = LocalDateTime.now();
        customers.add(new Customer(1, "Поздняков Владислав Оуджибудов", "+71111111111", null, now));
        customers.add(new Customer(2, "Халяпин Алексей Оуджибудов", "+72222222222", null, now));
        customers.add(new Customer(3, "Степан Степанов Оуджибудов", "+73333333333", null, now));
        customers.add(new Customer(4, "Костик Админ Оуджибудов", "+74444444444", null, now));
        customers.add(new Customer(5, "Воло Ботанов Оуджибудов", "+75555555555", null, now));
        customers.add(new Customer(6, "Владислав Кураков Оуджибудов", "+76666666666", null, now));
        customers.add(new Customer(7, "Никита Киков Оуджибудов", "+77777777777", null, now));
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(customers);
    }

    @Override
    public Customer create(Customer customer) {
        validate(customer);
        Customer saved = new Customer(nextId++, customer.fullName(), customer.phone(),
                customer.email(), LocalDateTime.now());
        customers.add(saved);
        return saved;
    }

    @Override
    public Customer update(Customer customer) {
        validate(customer);
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).id() == customer.id()) {
                customers.set(i, customer);
                return customer;
            }
        }
        throw new ValidationException("Заявитель не найден, возможно, он уже удалён");
    }

    @Override
    public void delete(int id) {
        boolean removed = customers.removeIf(customer -> customer.id() == id);
        if (!removed) {
            throw new ValidationException("Заявитель не найден, возможно, он уже удалён");
        }
    }

    private void validate(Customer customer) {
        String fullName = customer.fullName() == null ? "" : customer.fullName().trim();
        if (fullName.isEmpty()) {
            throw new ValidationException("fullName", "ФИО: поле обязательно для заполнения");
        }
        if (fullName.length() > 150) {
            throw new ValidationException("fullName", "ФИО: не длиннее 150 символов");
        }
        if (!FULL_NAME_PATTERN.matcher(fullName).matches()) {
            throw new ValidationException("fullName", "ФИО: введите три слова через пробел, только буквы");
        }

        String phone = customer.phone() == null ? "" : customer.phone().trim();
        if (phone.isEmpty()) {
            throw new ValidationException("phone", "Телефон: поле обязательно для заполнения");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("phone", "Телефон: введите номер в формате +7XXXXXXXXXX");
        }

        String email = customer.email();
        if (email != null) {
            if (email.length() > 100) {
                throw new ValidationException("email", "Email: не длиннее 100 символов");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ValidationException("email", "Email: введите корректный адрес, например name@mail.ru");
            }
        }
    }
}