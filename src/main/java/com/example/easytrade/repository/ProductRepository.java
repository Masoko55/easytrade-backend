package com.example.easytrade.repository;

import com.example.easytrade.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Example for searching by name (case-insensitive) and returning a page
    // You'll need to adapt this query based on your search requirements
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    // If query is empty, find all
    Page<Product> findAll(Pageable pageable);
}