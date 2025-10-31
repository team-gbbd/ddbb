package com.ddbb.config;

import com.ddbb.entity.management.Bread;
import com.ddbb.entity.management.Inventory;
import com.ddbb.entity.management.Sales;
import com.ddbb.repository.management.BreadRepository;
import com.ddbb.repository.management.InventoryRepository;
import com.ddbb.repository.management.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 테스트용 샘플 데이터 초기화
 * 실제 운영 시에는 이 클래스를 비활성화하거나 삭제하세요
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final BreadRepository breadRepository;
    private final InventoryRepository inventoryRepository;
    private final SalesRepository salesRepository;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 이미 데이터가 있으면 초기화하지 않음
            if (breadRepository.count() > 0) {
                System.out.println("데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
                return;
            }
            
            System.out.println("샘플 데이터 초기화 시작...");
            
            // 1. 빵 데이터 생성
            List<Bread> breads = createBreadData();
            breadRepository.saveAll(breads);
            System.out.println("빵 데이터 " + breads.size() + "개 생성 완료");
            
            // 2. 재고 데이터 생성
            List<Inventory> inventories = createInventoryData(breads);
            inventoryRepository.saveAll(inventories);
            System.out.println("재고 데이터 " + inventories.size() + "개 생성 완료");
            
            // 3. 판매 데이터 생성 (최근 30일)
            List<Sales> salesList = createSalesData(breads);
            salesRepository.saveAll(salesList);
            System.out.println("판매 데이터 " + salesList.size() + "개 생성 완료");
            
            System.out.println("샘플 데이터 초기화 완료!");
        };
    }
    
    private List<Bread> createBreadData() {
        List<Bread> breads = new ArrayList<>();
        
        breads.add(Bread.builder()
                .name("크루아상")
                .description("버터 풍미가 가득한 프랑스식 크루아상")
                .price(new BigDecimal("3000"))
                .category("페이스트리")
                .imageUrl("/images/croissant.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("바게트")
                .description("겉은 바삭하고 속은 부드러운 프랑스 빵")
                .price(new BigDecimal("2000"))
                .category("하드빵")
                .imageUrl("/images/baguette.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("단팥빵")
                .description("달콤한 팥소가 가득한 전통 빵")
                .price(new BigDecimal("2500"))
                .category("소프트빵")
                .imageUrl("/images/anpan.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("소금빵")
                .description("겉은 짭조름하고 속은 부드러운 인기 빵")
                .price(new BigDecimal("1500"))
                .category("소프트빵")
                .imageUrl("/images/salt_bread.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("초코 머핀")
                .description("초콜릿 칩이 가득한 달콤한 머핀")
                .price(new BigDecimal("3500"))
                .category("머핀")
                .imageUrl("/images/choco_muffin.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("치아바타")
                .description("이탈리아식 식사빵")
                .price(new BigDecimal("4000"))
                .category("하드빵")
                .imageUrl("/images/ciabatta.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("크림빵")
                .description("부드러운 커스터드 크림이 가득")
                .price(new BigDecimal("2800"))
                .category("소프트빵")
                .imageUrl("/images/cream_bread.jpg")
                .build());
        
        breads.add(Bread.builder()
                .name("식빵")
                .description("부드럽고 촉촉한 우유 식빵")
                .price(new BigDecimal("5000"))
                .category("식빵")
                .imageUrl("/images/milk_bread.jpg")
                .build());
        
        return breads;
    }
    
    private List<Inventory> createInventoryData(List<Bread> breads) {
        List<Inventory> inventories = new ArrayList<>();
        Random random = new Random();
        
        for (Bread bread : breads) {
            inventories.add(Inventory.builder()
                    .bread(bread)
                    .quantity(random.nextInt(50) + 20) // 20~70 사이의 재고
                    .minStockLevel(10 + random.nextInt(10)) // 10~20 사이의 최소 재고
                    .lastRestockedAt(LocalDateTime.now().minusDays(random.nextInt(5)))
                    .build());
        }
        
        return inventories;
    }
    
    private List<Sales> createSalesData(List<Bread> breads) {
        List<Sales> salesList = new ArrayList<>();
        Random random = new Random();
        
        // 최근 30일간의 판매 데이터 생성
        for (int day = 0; day < 30; day++) {
            LocalDateTime saleDate = LocalDateTime.now().minusDays(day);
            
            // 하루에 10~30개의 판매 기록 생성
            int salesCount = 10 + random.nextInt(21);
            
            for (int i = 0; i < salesCount; i++) {
                Bread bread = breads.get(random.nextInt(breads.size()));
                int quantity = 1 + random.nextInt(5); // 1~5개 판매
                BigDecimal totalPrice = bread.getPrice().multiply(BigDecimal.valueOf(quantity));
                
                salesList.add(Sales.builder()
                        .bread(bread)
                        .quantity(quantity)
                        .totalPrice(totalPrice)
                        .saleDate(saleDate.minusHours(random.nextInt(12)))
                        .build());
            }
        }
        
        return salesList;
    }
}

