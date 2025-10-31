package com.ddbb.repository.management;

import com.ddbb.entity.management.Bread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreadRepository extends JpaRepository<Bread, Long> {
    List<Bread> findByCategory(String category);
    List<Bread> findByNameContaining(String name);
}

