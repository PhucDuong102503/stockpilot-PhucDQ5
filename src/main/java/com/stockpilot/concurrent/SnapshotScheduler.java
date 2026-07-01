package com.stockpilot.concurrent;

import com.stockpilot.model.Product;
import com.stockpilot.service.ReportService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically creates snapshots of sales reports and stock status using a background thread.
 * Employs a Graceful Shutdown mechanism.
 */
public class SnapshotScheduler {

    private final ReportService reportService;
    private final ScheduledExecutorService scheduler;
    private static final String SNAPSHOT_FILE = "output/report_snapshot.txt";

    public SnapshotScheduler(ReportService reportService) {
        this.reportService = reportService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Snapshot-Background-Thread");
            thread.setDaemon(true); // Daemon thread so it doesn't block JVM exit
            return thread;
        });
        
        // Ensure output folder exists
        File dir = new File("output");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Starts the periodic background reporting task.
     * @param periodSeconds Period between subsequent snapshot writes.
     */
    public void start(int periodSeconds) {
        scheduler.scheduleAtFixedRate(this::writeSnapshot, 0, periodSeconds, TimeUnit.SECONDS);
        System.out.println("[Scheduler] Background report snapshot scheduler started (" + periodSeconds + "s interval).");
    }

    /**
     * Stops the background thread pool gracefully.
     */
    public void stop() {
        System.out.println("[Scheduler] Initiating shutdown for Background snapshot thread...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println("[Scheduler] Background snapshot thread stopped cleanly.");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void writeSnapshot() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(dtf);

        // Fetch metrics using Stream API via ReportService
        Map<String, BigDecimal> categoryRevenue = reportService.getRevenueByCategory();
        List<Product> lowStock = reportService.getLowStockProducts(10); // alert limit of 10 items

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("         SYSTEM REPORT SNAPSHOT (BACKGROUND)         \n");
        sb.append("====================================================\n");
        sb.append("Timestamp: ").append(timestamp).append("\n");
        sb.append("----------------------------------------------------\n\n");
        
        sb.append("1. REVENUE BY CATEGORY:\n");
        if (categoryRevenue.isEmpty()) {
            sb.append("  No sales records found.\n");
        } else {
            categoryRevenue.forEach((category, revenue) -> {
                BigDecimal r = revenue != null ? revenue : BigDecimal.ZERO;
                sb.append(String.format("  %-15s : $%s%n", category, r));
            });
        }
        sb.append("\n");

        sb.append("2. LOW-STOCK ALERT (Threshold <= 10):\n");
        if (lowStock.isEmpty()) {
            sb.append("  All products have healthy stock levels.\n");
        } else {
            lowStock.forEach(p -> 
                sb.append(String.format("  [%s] %-20s - stock: %d%n", p.getSku(), p.getName(), p.getStockQuantity()))
            );
        }
        sb.append("\n====================================================\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SNAPSHOT_FILE))) {
            writer.write(sb.toString());
            // System.out.println("[Background Thread] Snapshot written successfully to: " + SNAPSHOT_FILE);
        } catch (IOException e) {
            System.err.println("[Background Thread Error] Failed to write report snapshot: " + e.getMessage());
        }
    }
}
