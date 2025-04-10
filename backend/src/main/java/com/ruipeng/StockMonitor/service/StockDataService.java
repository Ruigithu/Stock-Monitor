package com.ruipeng.StockMonitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.StockMonitor.entity.StockPriceEntity;
import com.ruipeng.StockMonitor.model.StockData;
import com.ruipeng.StockMonitor.model.StockPrice;
import com.ruipeng.StockMonitor.repo.StockPriceRepo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockDataService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String  key = "08CON7JAMNAWKMA7";

    @Autowired
    private StockPriceRepo priceRepo;

    @Autowired
    private RedisService redisService;


    public StockData getStockData(String symbol) {
        String cacheKey = "stock_data_" + symbol;
        StockData cachedData = (StockData) redisService.get(cacheKey);
        if (cachedData != null) {
            System.out.println("Cache hit for: " + symbol);
            return cachedData;
        }
        System.out.println("Cache miss for: " + symbol);

        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + key;
        Request request = new Request.Builder().url(url).build();
        try (Response response=client.newCall(request).execute()){
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String jsonData = response.body().string();
            JsonNode root = mapper.readTree(jsonData);

            JsonNode metaDataNode = root.get("Meta Data");
            Map<String, String> metaData = new HashMap<>();
            metaDataNode.fields().forEachRemaining(entry-> metaData.put(entry.getKey(), entry.getValue().toString()));

            JsonNode stockPriceNode = root.get("Time Series (Daily)");
            Map<String, StockPrice> timeSeries = new HashMap<>();
            stockPriceNode.fields().forEachRemaining(entry->{
                StockPrice price = new StockPrice();
                price.setDate(entry.getKey());
                JsonNode values = entry.getValue();
                price.setOpen(values.get("1. open").asDouble());
                price.setHigh(values.get("2. high").asDouble());
                price.setLow(values.get("3. low").asDouble());
                price.setClose(values.get("4. close").asDouble());
                price.setVolume(values.get("5. volume").asLong());
                timeSeries.put(entry.getKey(), price);

                StockPriceEntity entity = new StockPriceEntity();
                entity.setId(symbol + "_" + entry.getKey());
                entity.setSymbol(symbol);
                entity.setDate(LocalDate.parse(entry.getKey()));
                entity.setOpen(price.getOpen());
                entity.setHigh(price.getHigh());
                entity.setLow(price.getLow());
                entity.setClose(price.getClose());
                entity.setVolume(price.getVolume());
                priceRepo.save(entity);

            });
            StockData stockData = new StockData();
            stockData.setMetaData(metaData);
            stockData.setTimeSeries(timeSeries);

            redisService.set(cacheKey, stockData, 60);
            return stockData;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
