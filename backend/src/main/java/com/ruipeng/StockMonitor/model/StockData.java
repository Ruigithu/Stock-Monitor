package com.ruipeng.StockMonitor.model;


import java.io.Serializable;
import java.util.Map;


public class StockData implements Serializable {
    private Map<String,String> metaData;
    private Map<String,StockPrice> timeSeries;

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, StockPrice> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(Map<String, StockPrice> timeSeries) {
        this.timeSeries = timeSeries;
    }
}
