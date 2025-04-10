package com.ruipeng.StockMonitor.service;

import com.ruipeng.StockMonitor.model.StockData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class StockMultiProcessorService {
    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private StockAnalyzeService stockAnalyzer;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public List<Map<String, Object>> processMultipleStocks(List<String> symbols) {
        List<Future<Map<String, Object>>> futures = new ArrayList<>();
        for (String symbol : symbols) {
            Future<Map<String, Object>> future = executorService.submit(() -> {
                try {
                    StockData stockData = stockDataService.getStockData(symbol);
                    double shortTermSMA = stockAnalyzer.calculateSMA(stockData.getTimeSeries(), 5);
                    double longTermSMA = stockAnalyzer.calculateSMA(stockData.getTimeSeries(), 20);
                    String signal = stockAnalyzer.generateTradingSignal(stockData.getTimeSeries(), symbol);

                    Map<String, Object> result = new HashMap<>();
                    result.put("symbol", symbol);
                    result.put("shortTermSMA", shortTermSMA);
                    result.put("longTermSMA", longTermSMA);
                    result.put("tradingSignal", signal);
                    System.out.println("Processed: " + symbol);
                    return result;
                } catch (Exception e) {
                    System.err.println("Error processing " + symbol + ": " + e.getMessage());
                    return null;
                }
            });
            futures.add(future);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> result = future.get();
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                System.err.println("Error retrieving result: " + e.getMessage());
            }
        }
        return results;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
