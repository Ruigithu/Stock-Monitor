package com.ruipeng.StockMonitor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.StockMonitor.model.StockData;
import com.ruipeng.StockMonitor.model.StockPrice;
import com.ruipeng.StockMonitor.repo.StockPriceRepo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class HandleStockData {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${alpha.vantage.secret-key}")
    private String  key;

    @Autowired
    private StockPriceRepo priceRepo;

    public StockData fetchDataFromApi(String symbol) throws IOException {
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + key;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String jsonData = response.body().string();
            JsonNode root = mapper.readTree(jsonData);

            StockData stockData = new StockData();

            // 1. MetaData
            JsonNode metaDataNode = root.get("Meta Data");
            Map<String, String> metaData = new HashMap<>();
            metaDataNode.fields().forEachRemaining(entry ->
                    metaData.put(entry.getKey(), entry.getValue().toString())
            );
            stockData.setMetaData(metaData);

            // 2. TimeSeries
            JsonNode timeSeriesNode = root.get("Time Series (Daily)");
            Map<String, StockPrice> timeSeries = new HashMap<>();
            timeSeriesNode.fields().forEachRemaining(entry -> {
                String date = entry.getKey();
                JsonNode priceNode = entry.getValue();

                StockPrice price = new StockPrice();
                price.setOpen(priceNode.get("1. open").asDouble());
                price.setHigh(priceNode.get("2. high").asDouble());
                price.setLow(priceNode.get("3. low").asDouble());
                price.setClose(priceNode.get("4. close").asDouble());
                price.setVolume(priceNode.get("5. volume").asLong());

                timeSeries.put(date, price);
            });

            stockData.setTimeSeries(timeSeries);

            return stockData;
        }
    }
}
