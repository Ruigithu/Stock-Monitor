package com.ruipeng.StockMonitor.service;

import com.ruipeng.StockMonitor.entity.StockAnalysisEntity;
import com.ruipeng.StockMonitor.model.StockPrice;
import com.ruipeng.StockMonitor.repo.StockAnalysisRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StockAnalyzeService {
    @Autowired
    private StockAnalysisRepo analysisRepo;
    public double calculateSMA(Map<String, StockPrice> timeSeries,int period){
        List<Double> closes = new ArrayList<>();
        timeSeries.values().stream()
                .sorted((p1,p2)->p2.getDate().compareTo(p1.getDate()))
                .limit(period)
                .forEach(p->closes.add(p.getClose()));
        return closes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public String generateTradingSignal(Map<String, StockPrice> timeSeries, String symbol) {
        double shortTermSMA = calculateSMA(timeSeries, 5);
        double longTermSMA = calculateSMA(timeSeries, 20);
        String signal ="Hold";

        if (shortTermSMA > longTermSMA) {
            signal= "Buy";
        } else if (shortTermSMA < longTermSMA) {
            signal= "Sell";
        }

        StockAnalysisEntity analysis = new StockAnalysisEntity();
        String latestDate = timeSeries.keySet().stream().max(String::compareTo).orElse("unknown");
        analysis.setId(symbol + "_" + latestDate);
        analysis.setSymbol(symbol);
        analysis.setDate(LocalDate.parse(latestDate));
        analysis.setShortTermSMA(shortTermSMA);
        analysis.setLongTermSMA(longTermSMA);
        analysis.setTradingSignal(signal);
        analysisRepo.save(analysis);

        return signal;
    }
}
