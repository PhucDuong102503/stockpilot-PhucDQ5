package com.stockpilot.model;

import com.stockpilot.exception.InvalidInputException;
import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private Long id;
    private Long orderId;
    private Product product;
    private int quantity;
    private BigDecimal price; // The price at which product was purchased

    public OrderItem() {}

    public OrderItem(Long id, Long orderId, Product product, int quantity, BigDecimal price) {
        this.id = id;
        this.orderId = orderId;
        setProduct(product);
        setQuantity(quantity);
        setPrice(price);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        if (product == null) {
            throw new InvalidInputException("Product in order item cannot be null.");
        }
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Price cannot be null or negative.");
        }
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id) && 
               Objects.equals(orderId, orderItem.orderId) &&
               Objects.equals(product, orderItem.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, product);
    }

    @Override
    public String toString() {
        return String.format("OrderItem{id=%d, orderId=%d, productSku='%s', quantity=%d, price=%s}",
                id, orderId, product != null ? product.getSku() : "null", quantity, price);
    }
}
