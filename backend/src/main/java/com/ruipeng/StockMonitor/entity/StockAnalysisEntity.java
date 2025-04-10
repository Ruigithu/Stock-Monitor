package com.ruipeng.StockMonitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "stock_analyses")
@Data
public class StockAnalysisEntity {
    @Id
    private String id; //  "symbol_date", "AAPL_2025-04-08"
    private String symbol;
    private LocalDate date;
    private double shortTermSMA;
    private double longTermSMA;
    private String tradingSignal;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getShortTermSMA() {
        return shortTermSMA;
    }

    public void setShortTermSMA(double shortTermSMA) {
        this.shortTermSMA = shortTermSMA;
    }

    public double getLongTermSMA() {
        return longTermSMA;
    }

    public void setLongTermSMA(double longTermSMA) {
        this.longTermSMA = longTermSMA;
    }

    public String getTradingSignal() {
        return tradingSignal;
    }

    public void setTradingSignal(String tradingSignal) {
        this.tradingSignal = tradingSignal;
    }
}
