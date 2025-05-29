package com.example.demo.service;

import com.example.demo.model.DishSeller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class DishSellerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DishSeller registerDishSeller(DishSeller dishSeller) {
        // Validate input
        if (dishSeller.getCompanyName() == null || dishSeller.getPassword() == null) {
            throw new IllegalArgumentException("Company name and password are required");
        }

        // Check if company name exists
        if (companyNameExists(dishSeller.getCompanyName())) {
            throw new IllegalArgumentException("Company name already exists");
        }

        // Insert new dish seller
        String insertSql = "INSERT INTO dish_seller (company_name, password) VALUES (?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(insertSql, Long.class,
                dishSeller.getCompanyName(),
                dishSeller.getPassword());

        dishSeller.setId(id);
        return dishSeller;
    }

    public DishSeller login(String companyName, String password) {
        String sql = "SELECT id, company_name, password FROM dish_seller WHERE company_name = ?";

        try {
            DishSeller dishSeller = jdbcTemplate.queryForObject(sql, new DishSellerRowMapper(), companyName);
            assert dishSeller != null;
            if (password.equals(dishSeller.getPassword())) {
                dishSeller.setPassword(null); // Don't return password
                return dishSeller;
            }
            throw new SecurityException("Invalid credentials");
        } catch (EmptyResultDataAccessException e) {
            throw new SecurityException("Invalid credentials");
        }
    }

    private boolean companyNameExists(String companyName) {
        String sql = "SELECT COUNT(*) FROM dish_seller WHERE company_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, companyName);
        return count != null && count > 0;
    }


    public List<DishSeller> getAllDishSellers() {
        String sql = "SELECT id, company_name FROM dish_seller"; // Don't select password
        return jdbcTemplate.query(sql, new RowMapper<DishSeller>() {
            @Override
            public DishSeller mapRow(ResultSet rs, int rowNum) throws SQLException {
                DishSeller dishSeller = new DishSeller();
                dishSeller.setId(rs.getLong("id"));
                dishSeller.setCompanyName(rs.getString("company_name"));
                return dishSeller;
            }
        });
    }


    private class DishSellerRowMapper implements RowMapper<DishSeller> {
        @Override
        public DishSeller mapRow(ResultSet rs, int rowNum) throws SQLException {
            DishSeller dishSeller = new DishSeller();
            dishSeller.setId(rs.getLong("id"));
            dishSeller.setCompanyName(rs.getString("company_name"));
            dishSeller.setPassword(rs.getString("password"));
            return dishSeller;
        }
    }
}