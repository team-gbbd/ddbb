package com.ddbb.service.management;

import com.ddbb.dto.management.BreadCreateRequest;
import com.ddbb.dto.management.BreadResponse;
import com.ddbb.dto.management.BreadUpdateRequest;
import com.ddbb.entity.management.Bread;
import com.ddbb.entity.management.Inventory;
import com.ddbb.repository.management.BreadRepository;
import com.ddbb.repository.management.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BreadService {
    
    private final BreadRepository breadRepository;
    private final InventoryRepository inventoryRepository;
    
    /**
     * 모든 빵 조회
     */
    public List<BreadResponse> getAllBreads() {
        return breadRepository.findAll().stream()
                .map(BreadResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 빵 조회
     */
    public BreadResponse getBreadById(Long id) {
        Bread bread = breadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("빵을 찾을 수 없습니다. ID: " + id));
        return BreadResponse.from(bread);
    }
    
    /**
     * 빵 등록 (재고도 함께 생성)
     */
    @Transactional
    public BreadResponse createBread(BreadCreateRequest request) {
        // 빵 생성
        Bread bread = Bread.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
        
        Bread savedBread = breadRepository.save(bread);
        
        // 재고 생성
        Inventory inventory = Inventory.builder()
                .bread(savedBread)
                .quantity(request.getInitialStock())
                .minStockLevel(request.getMinStockLevel())
                .lastRestockedAt(LocalDateTime.now())
                .build();
        
        inventoryRepository.save(inventory);
        
        return BreadResponse.from(savedBread);
    }
    
    /**
     * 빵 정보 업데이트
     */
    @Transactional
    public BreadResponse updateBread(Long id, BreadUpdateRequest request) {
        Bread bread = breadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("빵을 찾을 수 없습니다. ID: " + id));
        
        // 빵 정보 업데이트
        bread.setName(request.getName());
        bread.setPrice(BigDecimal.valueOf(request.getPrice()));
        bread.setDescription(request.getDescription());
        
        Bread updatedBread = breadRepository.save(bread);
        return BreadResponse.from(updatedBread);
    }
}

