package com.ddbb.controller.management;

import com.ddbb.dto.management.BreadCreateRequest;
import com.ddbb.dto.management.BreadResponse;
import com.ddbb.dto.management.BreadUpdateRequest;
import com.ddbb.service.management.BreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BreadController {
    
    private final BreadService breadService;
    
    /**
     * 모든 빵 조회
     * GET /api/breads
     */
    @GetMapping
    public ResponseEntity<List<BreadResponse>> getAllBreads() {
        List<BreadResponse> breads = breadService.getAllBreads();
        return ResponseEntity.ok(breads);
    }
    
    /**
     * 특정 빵 조회
     * GET /api/breads/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BreadResponse> getBreadById(@PathVariable Long id) {
        BreadResponse bread = breadService.getBreadById(id);
        return ResponseEntity.ok(bread);
    }
    
    /**
     * 빵 등록
     * POST /api/breads
     */
    @PostMapping
    public ResponseEntity<BreadResponse> createBread(@Valid @RequestBody BreadCreateRequest request) {
        BreadResponse bread = breadService.createBread(request);
        return ResponseEntity.ok(bread);
    }
    
    /**
     * 빵 정보 수정
     * PUT /api/breads/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BreadResponse> updateBread(
            @PathVariable Long id,
            @Valid @RequestBody BreadUpdateRequest request) {
        BreadResponse bread = breadService.updateBread(id, request);
        return ResponseEntity.ok(bread);
    }
}

