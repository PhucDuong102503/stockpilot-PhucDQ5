package com.stockpilot.concurrent;

import com.stockpilot.model.Customer;
import com.stockpilot.model.Product;
import com.stockpilot.service.CustomerService;
import com.stockpilot.service.OrderService;
import com.stockpilot.service.ProductService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates a high-concurrency Flash Sale scenario to demonstrate Race Conditions
 * and Thread Safety.
 */
public class FlashSaleSimulator {

    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;

    public FlashSaleSimulator(OrderService orderService, ProductService productService, CustomerService customerService) {
        this.orderService = orderService;
        this.productService = productService;
        this.customerService = customerService;
    }

    /**
     * Simulates concurrent purchase of a product.
     * @param productSku The target product SKU
     * @param totalBuyers Total concurrent threads trying to buy
     * @param quantityPerBuyer Quantity each buyer wants to purchase
     * @param threadSafe True to run using ReentrantLock, False to run without synchronization
     */
    public void runSimulation(String productSku, int totalBuyers, int quantityPerBuyer, boolean threadSafe) {
        System.out.println("\n================ FLASH SALE SIMULATION ================");
        System.out.println("Target Product SKU: " + productSku);
        System.out.println("Concurrent Buyers:  " + totalBuyers);
        System.out.println("Quantity/Buyer:     " + quantityPerBuyer);
        System.out.println("Concurrency Guard:  " + (threadSafe ? "ENABLED (Thread-Safe)" : "DISABLED (Unsafe)"));
        System.out.println("=======================================================");

        // Fetch or create a simulation customer
        Customer buyer = customerService.listAllCustomers().stream()
                .findFirst()
                .orElseGet(() -> customerService.addCustomer(new Customer(null, "Flash Buyer", "flash@example.com", "090-000-0000")));

        Product product = productService.getProductBySku(productSku);
        int initialStock = product.getStockQuantity();
        System.out.println("[Init] Starting Stock: " + initialStock);

        ExecutorService executor = Executors.newFixedThreadPool(15);
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failureCounter = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalBuyers; i++) {
            executor.submit(() -> {
                try {
                    if (threadSafe) {
                        orderService.directCheckoutThreadSafe(buyer.getId(), productSku, quantityPerBuyer);
                    } else {
                        orderService.directCheckoutUnsafe(buyer.getId(), productSku, quantityPerBuyer);
                    }
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    failureCounter.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        Product finalProduct = productService.getProductBySku(productSku);
        int finalStock = finalProduct.getStockQuantity();

        System.out.println("\n----------------- SIMULATION REPORT -----------------");
        System.out.println("Duration:            " + (endTime - startTime) + " ms");
        System.out.println("Success Purchases:   " + successCounter.get());
        System.out.println("Failed Purchases:    " + failureCounter.get());
        System.out.println("Initial Stock:       " + initialStock);
        System.out.println("Final Stock in DB:   " + finalStock);

        int theoreticalStock = initialStock - (successCounter.get() * quantityPerBuyer);
        System.out.println("Theoretical Stock:   " + theoreticalStock);

        if (finalStock != theoreticalStock) {
            System.err.println("[CRITICAL ALERT] RACE CONDITION DETECTED! Database stock does not match successful transactions.");
        } else {
            System.out.println("[SUCCESS] Database stock is fully consistent.");
        }
        System.out.println("=====================================================");
    }
}
