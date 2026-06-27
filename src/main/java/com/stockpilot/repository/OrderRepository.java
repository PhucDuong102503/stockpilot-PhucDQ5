package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.util.DatabaseConnection;

import com.stockpilot.model.Customer;
import com.stockpilot.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for Order entities.
 * Includes transactional logic for saving an order and its items.
 */
public class OrderRepository implements Repository<Order, Long> {

    private static final String INSERT_ORDER_SQL =
            "INSERT INTO orders (customer_id, order_date, discount_amount, total_amount) VALUES (?, ?, ?, ?)";
    private static final String INSERT_ORDER_ITEM_SQL =
            "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
    
    private static final String SELECT_BASE_SQL =
            "SELECT o.id AS order_id, o.order_date, o.discount_amount, o.total_amount, " +
            "       c.id AS customer_id, c.name AS customer_name, c.email AS customer_email, c.phone AS customer_phone, " +
            "       oi.id AS item_id, oi.quantity, oi.price AS item_price, " +
            "       p.id AS product_id, p.sku AS product_sku, p.name AS product_name, p.category AS product_category, p.price AS product_price, p.stock_quantity " +
            "FROM orders o " +
            "JOIN customers c ON o.customer_id = c.id " +
            "LEFT JOIN order_items oi ON o.id = oi.order_id " +
            "LEFT JOIN products p ON oi.product_id = p.id ";

    // Dependencies
    private final ProductRepository productRepository;

    public OrderRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Saves an Order and its OrderItems in a single JDBC Transaction.
     * Also updates product stock quantities.
     */
    @Override
    public Order save(Order order) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Insert Order
            try (PreparedStatement psOrder = conn.prepareStatement(INSERT_ORDER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setLong(1, order.getCustomer().getId());
                psOrder.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
                psOrder.setBigDecimal(3, order.getDiscountAmount());
                psOrder.setBigDecimal(4, order.getTotalAmount());
                
                psOrder.executeUpdate();
                
                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setId(keys.getLong(1));
                    } else {
                        throw new DataAccessException("Failed to generate order ID");
                    }
                }
            }

            // 2. Insert OrderItems and Update Stock
            try (PreparedStatement psItem = conn.prepareStatement(INSERT_ORDER_ITEM_SQL)) {
                for (OrderItem item : order.getItems()) {
                    // Update stock
                    int newStock = item.getProduct().getStockQuantity() - item.getQuantity();
                    productRepository.updateStock(conn, item.getProduct().getId(), newStock);

                    // Insert item
                    psItem.setLong(1, order.getId());
                    psItem.setLong(2, item.getProduct().getId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getPrice());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            // Commit transaction
            conn.commit();
            return order;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Log the rollback exception but throw the original one
                    e.addSuppressed(ex);
                }
            }
            throw new DataAccessException("Transaction failed. Order rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    throw new DataAccessException("Failed to close connection", ex);
                }
            }
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = SELECT_BASE_SQL + "WHERE o.id = ? ORDER BY oi.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                Order order = null;
                while (rs.next()) {
                    if (order == null) {
                        order = mapOrder(rs);
                    }
                    OrderItem item = mapOrderItem(rs, order.getId());
                    if (item != null) {
                        order.addItem(item);
                    }
                }
                return Optional.ofNullable(order);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find order by id: " + id, e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = SELECT_BASE_SQL + "ORDER BY o.id, oi.id";
        Map<Long, Order> orderMap = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Long orderId = rs.getLong("order_id");
                Order order = orderMap.computeIfAbsent(orderId, k -> {
                    try {
                        return mapOrder(rs);
                    } catch (SQLException e) {
                        throw new DataAccessException("Failed to map order row", e);
                    }
                });
                
                OrderItem item = mapOrderItem(rs, orderId);
                if (item != null) {
                    order.addItem(item);
                }
            }
            return new ArrayList<>(orderMap.values());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve all orders", e);
        }
    }

    @Override
    public Order update(Order entity) {
        throw new UnsupportedOperationException("Orders cannot be updated after creation");
    }

    @Override
    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("Orders cannot be deleted");
    }

    // ─── Row Mappers ──────────────────────────────────────────────────────────

    private Order mapOrder(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getLong("customer_id"));
        c.setName(rs.getString("customer_name"));
        c.setEmail(rs.getString("customer_email"));
        c.setPhone(rs.getString("customer_phone"));

        Order o = new Order();
        o.setId(rs.getLong("order_id"));
        o.setCustomer(c);
        o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        return o;
    }

    private OrderItem mapOrderItem(ResultSet rs, Long orderId) throws SQLException {
        long itemId = rs.getLong("item_id");
        if (rs.wasNull() || itemId == 0) {
            return null;
        }

        Product p = new Product();
        p.setId(rs.getLong("product_id"));
        p.setSku(rs.getString("product_sku"));
        p.setName(rs.getString("product_name"));
        p.setCategory(rs.getString("product_category"));
        p.setPrice(rs.getBigDecimal("product_price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));

        OrderItem item = new OrderItem();
        item.setId(itemId);
        item.setOrderId(orderId);
        item.setProduct(p);
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getBigDecimal("item_price"));
        return item;
    }
}
