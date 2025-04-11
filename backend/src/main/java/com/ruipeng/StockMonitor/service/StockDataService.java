package com.ruipeng.StockMonitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.StockMonitor.entity.StockPriceEntity;
import com.ruipeng.StockMonitor.model.StockData;
import com.ruipeng.StockMonitor.model.StockPrice;
import com.ruipeng.StockMonitor.repo.StockPriceRepo;
import com.ruipeng.StockMonitor.util.HandleStockData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockDataService {

    @Value("${alpha.vantage.secret-key}")
    private String  key;

    @Autowired
    private StockPriceRepo priceRepo;

    @Autowired
    private RedisService redisService;

    @Autowired
    private HandleStockData handleStockData;


    public StockData getStockData(String symbol) throws IOException {
        String cacheKey = "stock_data_" + symbol;
        StockData cachedData = (StockData) redisService.get(cacheKey);
        if (cachedData != null) {
            System.out.println("Cache hit for: " + symbol);
            return cachedData;
        }
        System.out.println("Cache miss for: " + symbol);

        try {
            StockData stockData = handleStockData.fetchDataFromApi(symbol);
            redisService.set(cacheKey, stockData, 60);
            return stockData;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
