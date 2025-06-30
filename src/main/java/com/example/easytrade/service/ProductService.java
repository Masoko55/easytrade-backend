package com.example.easytrade.service;

import com.example.easytrade.model.Product;
import com.example.easytrade.repository.ProductRepository;
// Removed unused User and UserRepository imports for now, unless you use them for seller validation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(Product product, String sellerUsername) { // Added sellerUsername parameter
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Product price must be positive.");
        }
        if (sellerUsername == null || sellerUsername.trim().isEmpty()) {
            // In a real app, you'd validate this sellerUsername exists,
            // but for now, we'll just require it's provided.
            throw new IllegalArgumentException("Seller username must be provided.");
        }

        product.setSellerUsername(sellerUsername); // Set the seller's username
        // Timestamps are handled by @PrePersist in Product entity now
        // product.setCreatedAt(LocalDateTime.now());
        // product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Page<Product> getAllProducts(String query, int page, int size, String[] sortInput) {
        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "createdAt"; // Default sort field

        if (sortInput != null && sortInput.length > 0) {
            // Assuming format "field,direction" e.g., "name,asc" or just "name" (defaults to asc)
            String[] parts = sortInput[0].split(",");
            sortField = parts[0];
            if (parts.length == 2 && parts[1].equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            } else {
                direction = Sort.Direction.ASC; // Default to ASC if direction not specified or invalid
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        if (query != null && !query.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query.trim(), pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getImageUrl() != null) {
            product.setImageUrl(productDetails.getImageUrl());
        }
        // Do not allow changing sellerUsername on update unless explicitly intended
        // if (productDetails.getSellerUsername() != null) {
        //     product.setSellerUsername(productDetails.getSellerUsername());
        // }

        // Timestamp handled by @PreUpdate in Product entity
        // product.setUpdatedAt(LocalDateTime.now()); 
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}