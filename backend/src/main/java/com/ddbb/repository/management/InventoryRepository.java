package com.ddbb.repository.management;

import com.ddbb.entity.management.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByBreadId(Long breadId);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStockLevel")
    List<Inventory> findLowStockItems();
    
    @Query("SELECT i FROM Inventory i JOIN FETCH i.bread")
    List<Inventory> findAllWithBread();
}

