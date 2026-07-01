package com.stockpilot.service;

import com.stockpilot.exception.InsufficientStockException;
import com.stockpilot.exception.InvalidInputException;
import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.repository.CustomerRepository;
import com.stockpilot.repository.OrderRepository;
import com.stockpilot.repository.ProductRepository;
import com.stockpilot.service.discount.DiscountPolicy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service for handling orders and the shopping cart.
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private DiscountPolicy discountPolicy;

    // Lock for thread-safe operations
    private final ReentrantLock checkoutLock = new ReentrantLock();

    // The cart maps SKU -> Quantity
    private final Map<String, Integer> cart = new HashMap<>();

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, 
                        CustomerRepository customerRepository, DiscountPolicy discountPolicy) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.discountPolicy = discountPolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public void addToCart(String sku, int quantity) {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than 0");
        }
        
        // Verify product exists and check stock before adding to cart (optimistic check)
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        int currentCartQty = cart.getOrDefault(sku, 0);
        if (currentCartQty + quantity > product.getStockQuantity()) {
            throw new InsufficientStockException("Not enough stock for " + sku + ". Available: " + product.getStockQuantity());
        }

        cart.put(sku, currentCartQty + quantity);
    }

    public void removeFromCart(String sku) {
        if (!cart.containsKey(sku)) {
            throw new InvalidInputException("Item not in cart");
        }
        cart.remove(sku);
    }

    public void clearCart() {
        cart.clear();
    }

    public Map<String, Integer> getCart() {
        return new HashMap<>(cart); // return copy
    }

    /**
     * Checks out the current cart for the given customer.
     * Starts a transaction in the repository.
     */
    public Order checkout(Long customerId) {
        if (cart.isEmpty()) {
            throw new InvalidInputException("Cart is empty");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ProductNotFoundException("Customer not found with id: " + customerId));

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String sku = entry.getKey();
            int qty = entry.getValue();

            Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found during checkout: " + sku));

            if (product.getStockQuantity() < qty) {
                throw new InsufficientStockException("Not enough stock for " + sku + ". Available: " + product.getStockQuantity());
            }

            OrderItem item = new OrderItem(null, null, product, qty, product.getPrice());
            order.addItem(item);
            
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal discountAmount = discountPolicy.calculateDiscount(subtotal);
        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal; // Cannot discount more than subtotal
        }
        
        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);

        // Save order and clear cart
        Order savedOrder = orderRepository.save(order);
        clearCart();
        return savedOrder;
    }

    /**
     * Direct checkout for a single product (UNSAFE).
     * Demonstrates race condition when multiple threads read & modify stock concurrently.
     */
    public Order directCheckoutUnsafe(Long customerId, String sku, int quantity) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ProductNotFoundException("Customer not found with id: " + customerId));

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock for " + sku);
        }

        // Artificial delay to widen the race condition window
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());

        OrderItem item = new OrderItem(null, null, product, quantity, product.getPrice());
        order.addItem(item);

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = discountPolicy.calculateDiscount(subtotal);
        BigDecimal total = subtotal.subtract(discount);

        order.setDiscountAmount(discount);
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    /**
     * Direct checkout for a single product (THREAD-SAFE).
     * Uses Java ReentrantLock to serialize purchase execution.
     */
    public Order directCheckoutThreadSafe(Long customerId, String sku, int quantity) {
        checkoutLock.lock();
        try {
            return directCheckoutUnsafe(customerId, sku, quantity);
        } finally {
            checkoutLock.unlock();
        }
    }
}
