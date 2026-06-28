package com.stockpilot.io;

import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Handles exporting invoices and report data to text files.
 */
public class InvoiceExporter {

    private static final String OUTPUT_DIR = "output";

    public InvoiceExporter() {
        // Ensure output directory exists
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Exports an order invoice to a formatted text file.
     */
    public String exportInvoice(Order order) throws IOException {
        String filename = String.format("%s/invoice_%d.txt", OUTPUT_DIR, order.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("====================================================\n");
            writer.write("                     INVOICE                        \n");
            writer.write("====================================================\n");
            writer.write(String.format("Order ID:      %d\n", order.getId()));
            writer.write(String.format("Order Date:    %s\n", order.getOrderDate().format(formatter)));
            writer.write("----------------------------------------------------\n");
            writer.write(String.format("Customer:      %s\n", order.getCustomer().getName()));
            writer.write(String.format("Email:         %s\n", order.getCustomer().getEmail()));
            writer.write(String.format("Phone:         %s\n", order.getCustomer().getPhone()));
            writer.write("====================================================\n");
            writer.write(String.format(" %-10s | %-20s | %-4s | %-10s\n", "SKU", "Name", "Qty", "Price"));
            writer.write("----------------------------------------------------\n");

            BigDecimal subtotal = BigDecimal.ZERO;
            for (OrderItem item : order.getItems()) {
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);
                writer.write(String.format(" %-10s | %-20s | %-4d | $%s\n",
                        item.getProduct().getSku(),
                        truncate(item.getProduct().getName(), 20),
                        item.getQuantity(),
                        item.getPrice()
                ));
            }
            writer.write("----------------------------------------------------\n");
            writer.write(String.format("Subtotal:                                  $%s\n", subtotal));
            writer.write(String.format("Discount:                                 -$%s\n", order.getDiscountAmount()));
            writer.write("----------------------------------------------------\n");
            writer.write(String.format("Total Paid:                                $%s\n", order.getTotalAmount()));
            writer.write("====================================================\n");
            writer.write("              Thank you for your business!          \n");
            writer.write("====================================================\n");
        }
        return filename;
    }

    /**
     * Exports raw report text to a file.
     */
    public String exportReport(String reportName, String content) throws IOException {
        String filename = String.format("%s/%s_%d.txt", OUTPUT_DIR, reportName, System.currentTimeMillis());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
        }
        return filename;
    }

    private String truncate(String val, int length) {
        if (val == null) return "";
        if (val.length() <= length) return val;
        return val.substring(0, length - 3) + "...";
    }
}
