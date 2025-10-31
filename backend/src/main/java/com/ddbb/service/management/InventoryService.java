package com.ddbb.service.management;

import com.ddbb.dto.management.InventoryResponse;
import com.ddbb.dto.management.InventoryUpdateRequest;
import com.ddbb.entity.management.Bread;
import com.ddbb.entity.management.Inventory;
import com.ddbb.repository.management.BreadRepository;
import com.ddbb.repository.management.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final BreadRepository breadRepository;
    
    /**
     * 모든 재고 조회
     */
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAllWithBread().stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 빵의 재고 조회
     */
    public InventoryResponse getInventoryByBreadId(Long breadId) {
        Inventory inventory = inventoryRepository.findByBreadId(breadId)
                .orElseThrow(() -> new RuntimeException("재고 정보를 찾을 수 없습니다. Bread ID: " + breadId));
        return InventoryResponse.from(inventory);
    }
    
    /**
     * 재고 부족 품목 조회
     */
    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 재고 업데이트
     */
    @Transactional
    public InventoryResponse updateInventory(Long breadId, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findByBreadId(breadId)
                .orElseGet(() -> {
                    // 재고가 없으면 새로 생성
                    Bread bread = breadRepository.findById(breadId)
                            .orElseThrow(() -> new RuntimeException("빵 정보를 찾을 수 없습니다. ID: " + breadId));
                    return Inventory.builder()
                            .bread(bread)
                            .quantity(0)
                            .build();
                });
        
        inventory.setQuantity(request.getQuantity());
        if (request.getMinStockLevel() != null) {
            inventory.setMinStockLevel(request.getMinStockLevel());
        }
        inventory.setLastRestockedAt(LocalDateTime.now());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return InventoryResponse.from(savedInventory);
    }
    
    /**
     * 재고 증가 (입고)
     */
    @Transactional
    public InventoryResponse increaseStock(Long breadId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByBreadId(breadId)
                .orElseThrow(() -> new RuntimeException("재고 정보를 찾을 수 없습니다. Bread ID: " + breadId));
        
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setLastRestockedAt(LocalDateTime.now());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return InventoryResponse.from(savedInventory);
    }
    
    /**
     * 재고 감소 (판매)
     */
    @Transactional
    public InventoryResponse decreaseStock(Long breadId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByBreadId(breadId)
                .orElseThrow(() -> new RuntimeException("재고 정보를 찾을 수 없습니다. Bread ID: " + breadId));
        
        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("재고가 부족합니다. 현재 재고: " + inventory.getQuantity());
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return InventoryResponse.from(savedInventory);
    }
}

