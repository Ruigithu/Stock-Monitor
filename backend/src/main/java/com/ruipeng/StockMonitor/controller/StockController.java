package com.ruipeng.StockMonitor.controller;

import com.ruipeng.StockMonitor.entity.StockAnalysisEntity;
import com.ruipeng.StockMonitor.model.StockData;
import com.ruipeng.StockMonitor.repo.StockAnalysisRepo;
import com.ruipeng.StockMonitor.service.StockAnalyzeService;
import com.ruipeng.StockMonitor.service.StockDataService;
import com.ruipeng.StockMonitor.service.StockMultiProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class StockController {
    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private StockAnalyzeService stockAnalyzeService;

    @Autowired
    private StockAnalysisRepo analysisRepo;
    @Autowired
    private StockMultiProcessorService stockMultiProcessorService;

    @GetMapping("/stock/daily")
    public StockData getDailyData(@RequestParam String symbol) throws IOException {
        return stockDataService.getStockData(symbol);
    }

    @GetMapping("/stock/analysis")
    public Map<String, Object> getStockAnalysis(@RequestParam String symbol) throws IOException {
        StockData stockData = stockDataService.getStockData(symbol);
        double shortTermSMA = stockAnalyzeService.calculateSMA(stockData.getTimeSeries(), 5);
        double longTermSMA = stockAnalyzeService.calculateSMA(stockData.getTimeSeries(), 20);
        String signal = stockAnalyzeService.generateTradingSignal(stockData.getTimeSeries(),symbol);

        Map<String, Object> result = new HashMap<>();
        result.put("symbol", symbol);
        result.put("shortTermSMA", shortTermSMA);
        result.put("longTermSMA", longTermSMA);
        result.put("tradingSignal", signal);
        return result;
    }
    @GetMapping("/stock/history")
    public List<StockAnalysisEntity> getStockHistory(@RequestParam String symbol) {
        return analysisRepo.findAll().stream()
                .filter(analysis -> analysis.getSymbol().equals(symbol))
                .collect(Collectors.toList());
    }
    @GetMapping("/stock/process-multiple")
    public List<Map<String, Object>> processMultipleStocks(@RequestParam String symbols) {
        List<String> symbolList = Arrays.asList(symbols.split(","));
        for(String symbol: symbolList){
            System.out.println(symbol);
        }
        return stockMultiProcessorService.processMultipleStocks(symbolList);
    }

}
