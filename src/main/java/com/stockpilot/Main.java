package com.stockpilot;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.InvalidInputException;
import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Product;
import com.stockpilot.repository.CustomerRepository;
import com.stockpilot.repository.ProductRepository;
import com.stockpilot.service.CustomerService;
import com.stockpilot.service.ProductService;
import com.stockpilot.util.DatabaseInitializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point and CLI menu loop for StockPilot.
 * No business logic and no SQL here — delegates everything to Service layer.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static ProductService productService;
    private static CustomerService customerService;

    public static void main(String[] args) {
        System.out.println("=== StockPilot — Inventory & Order Management System ===");

        // 1. Initialize DB (creates tables if they don't exist)
        try {
            DatabaseInitializer.initialize();
        } catch (DataAccessException e) {
            System.err.println("[ERROR] Could not initialize database: " + e.getMessage());
            return;
        }

        // 2. Wire up repositories and services
        productService  = new ProductService(new ProductRepository());
        customerService = new CustomerService(new CustomerRepository());

        // 3. Main menu loop
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> productMenu();
                case "2" -> customerMenu();
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default  -> System.out.println("[!] Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    // ─── Menus ────────────────────────────────────────────────────────────────

    private static void printMainMenu() {
        System.out.println("\n╔══════════════════════════╗");
        System.out.println("║       MAIN MENU          ║");
        System.out.println("╠══════════════════════════╣");
        System.out.println("║  1. Product Management   ║");
        System.out.println("║  2. Customer Management  ║");
        System.out.println("║  0. Exit                 ║");
        System.out.println("╚══════════════════════════╝");
        System.out.print("Choose: ");
    }

    // ─── Product Management ───────────────────────────────────────────────────

    private static void productMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Product Management ---");
            System.out.println("  1. List all products");
            System.out.println("  2. Find product by ID");
            System.out.println("  3. Add new product");
            System.out.println("  4. Update product");
            System.out.println("  5. Delete product");
            System.out.println("  6. Adjust stock quantity");
            System.out.println("  7. Low-stock report");
            System.out.println("  0. Back to main menu");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> listAllProducts();
                    case "2" -> findProductById();
                    case "3" -> addProduct();
                    case "4" -> updateProduct();
                    case "5" -> deleteProduct();
                    case "6" -> adjustStock();
                    case "7" -> lowStockReport();
                    case "0" -> back = true;
                    default  -> System.out.println("[!] Invalid option.");
                }
            } catch (InvalidInputException e) {
                System.out.println("[Validation Error] " + e.getMessage());
            } catch (ProductNotFoundException e) {
                System.out.println("[Not Found] " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("[DB Error] " + e.getMessage());
            }
        }
    }

    private static void listAllProducts() {
        List<Product> products = productService.listAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.println("\n ID  | SKU       | Name                     | Category    | Price       | Stock");
        System.out.println("-----|-----------|--------------------------|-------------|-------------|------");
        products.forEach(p -> System.out.printf(" %-4d| %-9s| %-24s | %-11s | %11s | %d%n",
                p.getId(), p.getSku(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQuantity()));
    }

    private static void findProductById() {
        System.out.print("Enter product ID: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Product p = productService.getProductById(id);
        System.out.println(p);
    }

    private static void addProduct() {
        System.out.println("-- Add New Product --");
        System.out.print("SKU (e.g. ABC-1234): ");
        String sku = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        System.out.print("Price: ");
        BigDecimal price = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Stock quantity: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());

        Product product = new Product(null, sku, name, category, price, stock);
        Product saved = productService.addProduct(product);
        System.out.println("[OK] Product saved with ID: " + saved.getId());
    }

    private static void updateProduct() {
        System.out.print("Enter product ID to update: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Product existing = productService.getProductById(id);

        System.out.println("Current: " + existing);
        System.out.print("New SKU (leave blank to keep '" + existing.getSku() + "'): ");
        String sku = scanner.nextLine().trim();
        System.out.print("New Name (leave blank to keep '" + existing.getName() + "'): ");
        String name = scanner.nextLine().trim();
        System.out.print("New Category (leave blank to keep '" + existing.getCategory() + "'): ");
        String category = scanner.nextLine().trim();
        System.out.print("New Price (leave blank to keep " + existing.getPrice() + "): ");
        String priceStr = scanner.nextLine().trim();
        System.out.print("New Stock (leave blank to keep " + existing.getStockQuantity() + "): ");
        String stockStr = scanner.nextLine().trim();

        if (!sku.isEmpty())      existing.setSku(sku);
        if (!name.isEmpty())     existing.setName(name);
        if (!category.isEmpty()) existing.setCategory(category);
        if (!priceStr.isEmpty()) existing.setPrice(new BigDecimal(priceStr));
        if (!stockStr.isEmpty()) existing.setStockQuantity(Integer.parseInt(stockStr));

        productService.updateProduct(existing);
        System.out.println("[OK] Product updated.");
    }

    private static void deleteProduct() {
        System.out.print("Enter product ID to delete: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        boolean deleted = productService.deleteProduct(id);
        System.out.println(deleted ? "[OK] Product deleted." : "[!] Product not found.");
    }

    private static void adjustStock() {
        System.out.print("Enter product ID: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Delta (positive = add stock, negative = remove): ");
        int delta = Integer.parseInt(scanner.nextLine().trim());
        Product updated = productService.adjustStock(id, delta);
        System.out.printf("[OK] New stock for '%s': %d%n", updated.getSku(), updated.getStockQuantity());
    }

    private static void lowStockReport() {
        System.out.print("Enter low-stock threshold: ");
        int threshold = Integer.parseInt(scanner.nextLine().trim());
        List<Product> lowStock = productService.getLowStockProducts(threshold);
        if (lowStock.isEmpty()) {
            System.out.println("No products below threshold " + threshold + ".");
        } else {
            System.out.println("\nLow-stock products (threshold <= " + threshold + "):");
            lowStock.forEach(p -> System.out.printf("  [%s] %s — stock: %d%n",
                    p.getSku(), p.getName(), p.getStockQuantity()));
        }
    }

    // ─── Customer Management ──────────────────────────────────────────────────

    private static void customerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Customer Management ---");
            System.out.println("  1. List all customers");
            System.out.println("  2. Find customer by ID");
            System.out.println("  3. Add new customer");
            System.out.println("  4. Update customer");
            System.out.println("  5. Delete customer");
            System.out.println("  0. Back to main menu");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> listAllCustomers();
                    case "2" -> findCustomerById();
                    case "3" -> addCustomer();
                    case "4" -> updateCustomer();
                    case "5" -> deleteCustomer();
                    case "0" -> back = true;
                    default  -> System.out.println("[!] Invalid option.");
                }
            } catch (InvalidInputException e) {
                System.out.println("[Validation Error] " + e.getMessage());
            } catch (ProductNotFoundException e) {
                System.out.println("[Not Found] " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("[DB Error] " + e.getMessage());
            }
        }
    }

    private static void listAllCustomers() {
        List<Customer> customers = customerService.listAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        System.out.println("\n ID  | Name                     | Email                        | Phone");
        System.out.println("-----|--------------------------|------------------------------|------------");
        customers.forEach(c -> System.out.printf(" %-4d| %-24s | %-28s | %s%n",
                c.getId(), c.getName(), c.getEmail(), c.getPhone()));
    }

    private static void findCustomerById() {
        System.out.print("Enter customer ID: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Customer c = customerService.getCustomerById(id);
        System.out.println(c);
    }

    private static void addCustomer() {
        System.out.println("-- Add New Customer --");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        Customer customer = new Customer(null, name, email, phone);
        Customer saved = customerService.addCustomer(customer);
        System.out.println("[OK] Customer saved with ID: " + saved.getId());
    }

    private static void updateCustomer() {
        System.out.print("Enter customer ID to update: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Customer existing = customerService.getCustomerById(id);

        System.out.println("Current: " + existing);
        System.out.print("New Name (leave blank to keep): ");
        String name = scanner.nextLine().trim();
        System.out.print("New Email (leave blank to keep): ");
        String email = scanner.nextLine().trim();
        System.out.print("New Phone (leave blank to keep): ");
        String phone = scanner.nextLine().trim();

        if (!name.isEmpty())  existing.setName(name);
        if (!email.isEmpty()) existing.setEmail(email);
        if (!phone.isEmpty()) existing.setPhone(phone);

        customerService.updateCustomer(existing);
        System.out.println("[OK] Customer updated.");
    }

    private static void deleteCustomer() {
        System.out.print("Enter customer ID to delete: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        boolean deleted = customerService.deleteCustomer(id);
        System.out.println(deleted ? "[OK] Customer deleted." : "[!] Customer not found.");
    }
}
