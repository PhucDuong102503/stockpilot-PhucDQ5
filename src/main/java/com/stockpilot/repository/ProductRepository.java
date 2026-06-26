package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Product;
import com.stockpilot.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed repository for Product entities.
 * All SQL lives here — service layer never touches SQL directly.
 */
public class ProductRepository implements Repository<Product, Long> {

    // ─── SQL Constants ────────────────────────────────────────────────────────
    private static final String INSERT_SQL =
            "INSERT INTO products (sku, name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM products WHERE id = ?";
    private static final String FIND_BY_SKU_SQL =
            "SELECT * FROM products WHERE sku = ?";
    private static final String FIND_ALL_SQL =
            "SELECT * FROM products ORDER BY id";
    private static final String UPDATE_SQL =
            "UPDATE products SET sku = ?, name = ?, category = ?, price = ?, stock_quantity = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM products WHERE id = ?";
    private static final String UPDATE_STOCK_SQL =
            "UPDATE products SET stock_quantity = ? WHERE id = ?";
    private static final String FIND_LOW_STOCK_SQL =
            "SELECT * FROM products WHERE stock_quantity <= ? ORDER BY stock_quantity";

    // ─── CRUD Operations ──────────────────────────────────────────────────────

    @Override
    public Product save(Product product) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save product: " + product.getSku(), e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
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
            throw new DataAccessException("Failed to find product by id: " + id, e);
        }
    }

    public Optional<Product> findBySku(String sku) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_SKU_SQL)) {

            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find product by SKU: " + sku, e);
        }
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve all products", e);
        }
    }

    @Override
    public Product update(Product product) {
        if (product.getId() == null) {
            throw new DataAccessException("Cannot update product without id");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setLong(6, product.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new ProductNotFoundException("Product not found with id: " + product.getId());
            }
            return product;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update product: " + product.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete product with id: " + id, e);
        }
    }

    // ─── Extra operations used by Service / Order processing ─────────────────

    /**
     * Update only the stock_quantity column — used during order checkout.
     * Should be called within an existing JDBC transaction (conn passed in).
     */
    public void updateStock(Connection conn, Long productId, int newQuantity) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STOCK_SQL)) {
            ps.setInt(1, newQuantity);
            ps.setLong(2, productId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new ProductNotFoundException("Product not found when updating stock: " + productId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update stock for product: " + productId, e);
        }
    }

    /**
     * Returns products whose stock_quantity is at or below the given threshold.
     */
    public List<Product> findLowStock(int threshold) {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_LOW_STOCK_SQL)) {

            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
            return products;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find low-stock products", e);
        }
    }

    // ─── Row mapping ──────────────────────────────────────────────────────────

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        return p;
    }
}
