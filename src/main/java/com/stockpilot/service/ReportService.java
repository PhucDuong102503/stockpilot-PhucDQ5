package com.stockpilot.service;

import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.repository.OrderRepository;
import com.stockpilot.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating reports and statistics using Java Stream API.
 */
public class ReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public ReportService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Calculates total revenue and order count within a time period (inclusive).
     */
    public Map<String, Object> getRevenueAndOrderCount(LocalDateTime start, LocalDateTime end) {
        List<Order> filteredOrders = orderRepository.findAll().stream()
                .filter(o -> !o.getOrderDate().isBefore(start) && !o.getOrderDate().isAfter(end))
                .toList();

        BigDecimal totalRevenue = filteredOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "orderCount", filteredOrders.size(),
                "totalRevenue", totalRevenue
        );
    }

    /**
     * Identifies the Top-N best-selling products by quantity sold.
     */
    public List<Map.Entry<Product, Integer>> getTopSellingProducts(int n) {
        return orderRepository.findAll().stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(n)
                .toList();
    }

    /**
     * Groups and sums revenue by product category.
     */
    public Map<String, BigDecimal> getRevenueByCategory() {
        return orderRepository.findAll().stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),
                        Collectors.mapping(
                                item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    /**
     * Filters products below or equal to a threshold using Stream API.
     */
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() <= threshold)
                .toList();
    }
}
