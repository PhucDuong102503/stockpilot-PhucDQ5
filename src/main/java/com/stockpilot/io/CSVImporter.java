package com.stockpilot.io;

import com.stockpilot.exception.InvalidInputException;
import com.stockpilot.model.Product;
import com.stockpilot.service.ProductService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles importing products from a CSV file using manual string parsing.
 */
public class CSVImporter {

    private final ProductService productService;

    public CSVImporter(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Imports products from a CSV file.
     * CSV format: sku,name,category,price,stockQuantity
     * @param filePath Path to the CSV file.
     * @return Number of successfully imported products.
     */
    public int importProducts(String filePath) throws IOException {
        int successCount = 0;
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                // Trim and skip empty lines or headers
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.toLowerCase().startsWith("sku")) {
                    continue; 
                }

                try {
                    Product product = parseLine(trimmedLine);
                    productService.addProduct(product);
                    successCount++;
                } catch (Exception e) {
                    System.err.printf("[Warning] Skipping line %d due to error: %s (Content: %s)%n", 
                            lineNumber, e.getMessage(), trimmedLine);
                }
            }
        }
        return successCount;
    }

    private Product parseLine(String line) {
        // Simple manual split by comma (ignoring commas inside quotes for simplicity, 
        // or split using simple regex to handle potential commas)
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        
        if (parts.length < 5) {
            throw new InvalidInputException("Insufficient columns. Must have 5 columns: sku, name, category, price, stockQuantity");
        }

        String sku = cleanField(parts[0]);
        String name = cleanField(parts[1]);
        String category = cleanField(parts[2]);
        BigDecimal price = new BigDecimal(cleanField(parts[3]));
        int stockQuantity = Integer.parseInt(cleanField(parts[4]));

        return new Product(null, sku, name, category, price, stockQuantity);
    }

    private String cleanField(String field) {
        if (field == null) return "";
        String trimmed = field.trim();
        // Remove enclosing double quotes if present
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
