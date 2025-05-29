package com.example.demo.service;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    public Customer registerCustomer(Customer customer) {
        // Validate input
        if (customer.getEmail() == null || customer.getPassword() == null || customer.getLocation() == null) {
            throw new IllegalArgumentException("Email, password and location are required");
        }

        // Check if email exists
        if (emailExists(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        customer.setBalance(200);

        // Insert new customer
        String insertSql = "INSERT INTO customer (username, email, password, location, balance) VALUES (?, ?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(insertSql, Long.class,
                customer.getUsername(),
                customer.getEmail(),
                customer.getPassword(),
                customer.getLocation(),
                customer.getBalance());


        customer.setId(id);
        return customer;
    }
    public Customer login(String email, String password) {
        // Make sure this matches your actual table name (case-sensitive)
        String sql = "SELECT id, username, email, password, location FROM customer WHERE email = ?";

        try {
            Customer customer = jdbcTemplate.queryForObject(sql, new CustomerRowMapper(), email);
            assert customer != null;
            if (password.equals(customer.getPassword())) {
                customer.setPassword(null); // Don't return password
                return customer;
            }
            throw new SecurityException("Invalid credentials");
        } catch (EmptyResultDataAccessException e) {
            throw new SecurityException("Invalid credentials");
        }
    }

    private boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM customer WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public float getCustomerBalance(Long customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getBalance)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
    }


    public void deductBalance(Long customerId, float amount) {
        // Fetch the customer
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (!customerOptional.isPresent()) {
            throw new RuntimeException("Customer not found");
        }

        Customer customer = customerOptional.get();
        float updatedBalance = customer.getBalance()-amount;
        customer.setBalance(updatedBalance);

        // Save the updated customer balance
        customerRepository.save(customer);
    }

    public Customer getCustomerById(Long id) {
        String sql = "SELECT id, username, email, password, location FROM customer WHERE id = ?";
        try {
            Customer customer = jdbcTemplate.queryForObject(sql, new CustomerRowMapper(), id);
            if (customer != null) {
                customer.setPassword(null); // Don't expose password
            }
            return customer;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Customer> getAllCustomers() {
        String sql = "SELECT id, username, email, location , balance FROM customer";
        return jdbcTemplate.query(sql, new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
                Customer customer = new Customer();
                customer.setId(rs.getLong("id"));
                customer.setUsername(rs.getString("username"));
                customer.setEmail(rs.getString("email"));
                customer.setLocation(rs.getString("location"));
                customer.setBalance(rs.getFloat("balance"));


                return customer;
            }
        });
    }
    private class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Customer customer = new Customer();
            customer.setId(rs.getLong("id"));
            customer.setUsername(rs.getString("username"));
            customer.setEmail(rs.getString("email"));
            customer.setPassword(rs.getString("password"));
            customer.setLocation(rs.getString("location"));


            return customer;
        }
    }

}