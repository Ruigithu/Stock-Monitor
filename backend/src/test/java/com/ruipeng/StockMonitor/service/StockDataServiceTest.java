package com.ruipeng.StockMonitor.service;

import com.ruipeng.StockMonitor.model.StockData;
import com.ruipeng.StockMonitor.model.StockPrice;
import com.ruipeng.StockMonitor.repo.StockPriceRepo;
import com.ruipeng.StockMonitor.util.HandleStockData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockDataServiceTest {
    @InjectMocks
    private  StockDataService stockDataService;

    @Mock
    private StockPriceRepo priceRepo;

    @Mock
    private RedisService redisService;

    @Mock
    private HandleStockData handleStockData;


    /**
     * get data from Redis, not call third party API
     */
    @Test
    void testGetStockData_cachedHit() throws IOException {
        String symbol = "AAPL";
        StockData mockCachedData = new StockData();

        Map<String, String> mockMetaData = new HashMap<>();
        mockMetaData.put("1. Information", "\"Daily Prices\"");
        mockMetaData.put("2. Symbol", "\"AAPL\"");
        mockMetaData.put("3. Last Refreshed", "\"2025-04-10\"");
        mockMetaData.put("4. Output Size", "\"Compact\"");
        mockMetaData.put("5. Time Zone", "\"US/Eastern\"");
        mockCachedData.setMetaData(mockMetaData);

        HashMap<String, StockPrice> mockTimeSeries = new HashMap<>();
        StockPrice mockPrice = new StockPrice();
        mockPrice.setOpen(189.0650);
        mockPrice.setHigh(194.7799);
        mockPrice.setLow(183.0000);
        mockPrice.setClose(190.4200);
        mockPrice.setVolume(121879981);
        mockTimeSeries.put("2024-04-10", mockPrice);
        mockCachedData.setTimeSeries(mockTimeSeries);

        when(redisService.get("stock_data_AAPL")).thenReturn(mockCachedData);

        StockData result = stockDataService.getStockData(symbol);

        assertEquals(mockCachedData, result);

        verify(redisService, times(1)).get("stock_data_" + symbol);
        verifyNoMoreInteractions(priceRepo);

    }

    /**
     * miss data in Redis, call from API
     */
    @Test
    void testGetStockData_cachedMiss_APIHit() throws IOException {
        String symbol = "AAPL";
        when(redisService.get("stock_data_AAPL")).thenReturn(null);


        StockData mockAPIData = new StockData();
        Map<String, String> mockMetaData = new HashMap<>();
        mockMetaData.put("1. Information", "\"Daily Prices\"");
        mockMetaData.put("2. Symbol", "\"AAPL\"");
        mockMetaData.put("3. Last Refreshed", "\"2025-04-10\"");
        mockMetaData.put("4. Output Size", "\"Compact\"");
        mockMetaData.put("5. Time Zone", "\"US/Eastern\"");
        mockAPIData.setMetaData(mockMetaData);

        HashMap<String, StockPrice> mockTimeSeries = new HashMap<>();
        StockPrice mockPrice = new StockPrice();
        mockPrice.setOpen(189.0650);
        mockPrice.setHigh(194.7799);
        mockPrice.setLow(183.0000);
        mockPrice.setClose(190.4200);
        mockPrice.setVolume(121879981);
        mockTimeSeries.put("2024-04-10", mockPrice);
        mockAPIData.setTimeSeries(mockTimeSeries);

        when(handleStockData.fetchDataFromApi(symbol)).thenReturn(mockAPIData);
        StockData result = stockDataService.getStockData(symbol);

        assertEquals(result, mockAPIData);
        verify(handleStockData, times(1)).fetchDataFromApi(symbol);
        verify(redisService,times(1)).set("stock_data_AAPL",result,60);

    }
}
