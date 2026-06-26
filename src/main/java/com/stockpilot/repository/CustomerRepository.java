package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Customer;
import com.stockpilot.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed repository for Customer entities.
 * All SQL lives here — service layer never touches SQL directly.
 */
public class CustomerRepository implements Repository<Customer, Long> {

    // ─── SQL Constants ────────────────────────────────────────────────────────
    private static final String INSERT_SQL =
            "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM customers WHERE id = ?";
    private static final String FIND_BY_EMAIL_SQL =
            "SELECT * FROM customers WHERE email = ?";
    private static final String FIND_ALL_SQL =
            "SELECT * FROM customers ORDER BY id";
    private static final String UPDATE_SQL =
            "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM customers WHERE id = ?";

    // ─── CRUD Operations ──────────────────────────────────────────────────────

    @Override
    public Customer save(Customer customer) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                }
            }
            return customer;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save customer: " + customer.getEmail(), e);
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find customer by id: " + id, e);
        }
    }

    public Optional<Customer> findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find customer by email: " + email, e);
        }
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }
            return customers;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve all customers", e);
        }
    }

    @Override
    public Customer update(Customer customer) {
        if (customer.getId() == null) {
            throw new DataAccessException("Cannot update customer without id");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setLong(4, customer.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new ProductNotFoundException("Customer not found with id: " + customer.getId());
            }
            return customer;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update customer: " + customer.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete customer with id: " + id, e);
        }
    }

    // ─── Row mapping ──────────────────────────────────────────────────────────

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        return c;
    }
}
