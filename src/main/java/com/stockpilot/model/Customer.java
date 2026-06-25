package com.stockpilot.model;

import com.stockpilot.exception.InvalidInputException;
import java.util.Objects;
import java.util.regex.Pattern;

public class Customer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s-]{8,20}$");

    private Long id;
    private String name;
    private String email;
    private String phone;

    public Customer() {}

    public Customer(Long id, String name, String email, String phone) {
        this.id = id;
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Invalid email format.");
        }
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidInputException("Invalid phone format (must be 8-20 digits, spaces or hyphens allowed).");
        }
        this.phone = phone.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', email='%s', phone='%s'}", id, name, email, phone);
    }
}
