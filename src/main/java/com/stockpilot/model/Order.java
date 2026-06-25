package com.stockpilot.model;

import com.stockpilot.exception.InvalidInputException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private Long id;
    private Customer customer;
    private LocalDateTime orderDate;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    public Order(Long id, Customer customer, LocalDateTime orderDate, BigDecimal discountAmount, BigDecimal totalAmount) {
        this.id = id;
        setCustomer(customer);
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now();
        setDiscountAmount(discountAmount);
        setTotalAmount(totalAmount);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidInputException("Customer cannot be null for an order.");
        }
        this.customer = customer;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new InvalidInputException("Cannot add null order item.");
        }
        this.items.add(item);
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Discount amount cannot be null or negative.");
        }
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Total amount cannot be null or negative.");
        }
        this.totalAmount = totalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Order{id=%d, customerName='%s', date=%s, itemsCount=%d, discount=%s, total=%s}",
                id, customer != null ? customer.getName() : "null", orderDate, items.size(), discountAmount, totalAmount);
    }
}
