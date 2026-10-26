package com.katu4a.cladbise.service;

import com.katu4a.cladbise.model.Customer;
import com.katu4a.cladbise.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomerValidatorTest {
    private Customer customer(String phone, String email) {
        return new Customer(null, "Иванов", phone, email, null);
    }
    @ParameterizedTest
    @ValueSource(strings = {"+79991234567", "89991234567", "+7 (999) 123-45-67", "8 (999) 123 45 67", " +7 999 123-45-67 "})
    void normalizesPhone(String phone) {
        assertEquals("+79991234567", CustomerValidator.validate(customer(phone, null)).phone());
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "+7999123456", "+799912345678", "+19991234567", "abc9991234567", "+7 (999 123-45-67", "79991234567"})
    void rejectsInvalidPhone(String phone) {
        assertEquals("phone", assertThrows(ValidationException.class,
                () -> CustomerValidator.validate(customer(phone, null))).getField());
    }
    @ParameterizedTest
    @ValueSource(strings = {"name@example.com", "name.surname+tag@sub.example.ru", " name@example.com "})
    void acceptsEmail(String email) {
        assertEquals(email.trim(), CustomerValidator.validate(customer("+79991234567", email)).email());
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void emailIsOptional(String email) {
        assertNull(CustomerValidator.validate(customer("+79991234567", email)).email());
    }
    @ParameterizedTest
    @ValueSource(strings = {"name", "name@", "@example.ru", "name@example", "name@@example.ru", "name x@example.ru", ".name@example.ru", "name..x@example.ru", "name@-example.ru", "name@example..ru"})
    void rejectsInvalidEmail(String email) {
        assertEquals("email", assertThrows(ValidationException.class,
                () -> CustomerValidator.validate(customer("+79991234567", email))).getField());
    }
    @Test void invalidContactsNeverReachApiOnCreateOrUpdate() throws Exception {
        try (var server = new TestBackend()) {
            var service = new HttpCustomerService(new ApiClient(server.url()));
            assertThrows(ValidationException.class, () -> service.create(customer("123", "name@example.com")));
            assertThrows(ValidationException.class, () -> service.update(customer("+79991234567", "invalid")));
            assertNull(server.path);
        }
    }
}
