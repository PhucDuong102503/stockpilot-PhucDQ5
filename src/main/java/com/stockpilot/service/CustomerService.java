package com.stockpilot.service;

import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Customer;
import com.stockpilot.repository.CustomerRepository;

import java.util.List;

/**
 * Business logic for customer management.
 * Sits between CLI and the JDBC repository — no SQL here.
 */
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /** Register a new customer. Returns the saved customer (with generated id). */
    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    /** Return all customers. */
    public List<Customer> listAllCustomers() {
        return customerRepository.findAll();
    }

    /** Find a customer by id; throws if not found. */
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Customer not found with id: " + id));
    }

    /** Find a customer by email; throws if not found. */
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ProductNotFoundException("Customer not found with email: " + email));
    }

    /** Update customer info. */
    public Customer updateCustomer(Customer customer) {
        return customerRepository.update(customer);
    }

    /** Delete a customer by id. Returns true if deleted. */
    public boolean deleteCustomer(Long id) {
        getCustomerById(id); // confirm exists first
        return customerRepository.deleteById(id);
    }
}
