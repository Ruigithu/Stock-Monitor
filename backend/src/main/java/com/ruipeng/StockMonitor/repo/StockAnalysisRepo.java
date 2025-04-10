package com.ruipeng.StockMonitor.repo;

import com.ruipeng.StockMonitor.entity.StockAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAnalysisRepo extends JpaRepository<StockAnalysisEntity, String> {
}
