package com.ddbb.controller.management;

import com.ddbb.dto.management.InventoryResponse;
import com.ddbb.dto.management.InventoryUpdateRequest;
import com.ddbb.service.management.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * 모든 재고 조회
     * GET /api/inventory
     */
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        List<InventoryResponse> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * 특정 빵의 재고 조회
     * GET /api/inventory/bread/{breadId}
     */
    @GetMapping("/bread/{breadId}")
    public ResponseEntity<InventoryResponse> getInventoryByBreadId(@PathVariable Long breadId) {
        InventoryResponse inventory = inventoryService.getInventoryByBreadId(breadId);
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * 재고 부족 품목 조회
     * GET /api/inventory/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        List<InventoryResponse> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }
    
    /**
     * 재고 업데이트
     * PUT /api/inventory/bread/{breadId}
     */
    @PutMapping("/bread/{breadId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long breadId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        InventoryResponse updated = inventoryService.updateInventory(breadId, request);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 재고 증가 (입고)
     * POST /api/inventory/bread/{breadId}/increase
     */
    @PostMapping("/bread/{breadId}/increase")
    public ResponseEntity<InventoryResponse> increaseStock(
            @PathVariable Long breadId,
            @RequestParam Integer quantity) {
        InventoryResponse updated = inventoryService.increaseStock(breadId, quantity);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 재고 감소
     * POST /api/inventory/bread/{breadId}/decrease
     */
    @PostMapping("/bread/{breadId}/decrease")
    public ResponseEntity<InventoryResponse> decreaseStock(
            @PathVariable Long breadId,
            @RequestParam Integer quantity) {
        InventoryResponse updated = inventoryService.decreaseStock(breadId, quantity);
        return ResponseEntity.ok(updated);
    }
}

