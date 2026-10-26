package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Customer;
import java.util.regex.Pattern;

public final class CustomerValidator {
    private static final Pattern PHONE = Pattern.compile(
            "(?:\\+7|8) ?(?:\\([0-9]{3}\\)|[0-9]{3}) ?[0-9]{3}[ -]?[0-9]{2}[ -]?[0-9]{2}");
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}");
    private CustomerValidator() {}

    public static Customer validate(Customer customer) {
        String name = customer.fullName() == null ? "" : customer.fullName().trim();
        if (name.isEmpty() || name.length() > 150)
            throw new ValidationException("fullName", "ФИО: заполните поле, не более 150 символов");
        String phone = customer.phone() == null ? "" : customer.phone().trim();
        if (!PHONE.matcher(phone).matches())
            throw new ValidationException("phone", "Телефон: введите 11 цифр с +7 или 8, например +7 (999) 123-45-67");
        String email = customer.email() == null ? "" : customer.email().trim();
        if (!email.isEmpty() && (email.length() > 100 || !EMAIL.matcher(email).matches()
                || email.indexOf('@') > 64))
            throw new ValidationException("email", "Email: введите адрес вида name@example.com, не более 100 символов");
        phone = phone.replaceAll("[ ()-]", "");
        if (phone.startsWith("8")) phone = "+7" + phone.substring(1);
        return new Customer(customer.id(), name, phone, email.isEmpty() ? null : email, customer.createdAt());
    }
}
