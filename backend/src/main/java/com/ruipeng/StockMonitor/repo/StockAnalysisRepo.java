package com.ruipeng.StockMonitor.repo;

import com.ruipeng.StockMonitor.entity.StockAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockAnalysisRepo extends JpaRepository<StockAnalysisEntity, String> {
}
