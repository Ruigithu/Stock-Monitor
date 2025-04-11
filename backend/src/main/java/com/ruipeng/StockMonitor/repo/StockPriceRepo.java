package com.ruipeng.StockMonitor.repo;

import com.ruipeng.StockMonitor.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StockPriceRepo extends JpaRepository<StockPriceEntity,String> {
}
