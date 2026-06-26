package com.stockpilot.service;

import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Product;
import com.stockpilot.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for product management.
 * Sits between CLI and the JDBC repository — no SQL here.
 */
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** Add a new product. Returns the saved product (with generated id). */
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    /** Return all products. */
    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    /** Find a product by its id; throws if not found. */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    /** Find a product by its SKU; throws if not found. */
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
    }

    /** Update an existing product. */
    public Product updateProduct(Product product) {
        return productRepository.update(product);
    }

    /** Delete a product by id. Returns true if deleted. */
    public boolean deleteProduct(Long id) {
        // Confirm it exists first to give a clear error message
        getProductById(id);
        return productRepository.deleteById(id);
    }

    /** Adjust stock quantity (add or subtract). */
    public Product adjustStock(Long id, int delta) {
        Product product = getProductById(id);
        int newQty = product.getStockQuantity() + delta;
        product.setStockQuantity(newQty); // validates non-negative via setter
        return productRepository.update(product);
    }

    /** Products whose stock is at or below threshold. */
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStock(threshold);
    }
}
