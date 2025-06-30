package com.example.easytrade.controller;

import com.example.easytrade.model.Product;
import com.example.easytrade.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Ensures @PathVariable is imported

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // POST /api/products - Create a new product
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product productRequest) {
        // TEMPORARY: Assign a sellerUsername if not provided by the client.
        // This should be replaced with logic to get the authenticated user's username.
        if (productRequest.getSellerUsername() == null || productRequest.getSellerUsername().trim().isEmpty()) {
            // In a real app with authentication, you'd get this from the security context:
            // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // String currentPrincipalName = authentication.getName();
            // productRequest.setSellerUsername(currentPrincipalName);
            productRequest.setSellerUsername("Masoko_TestSeller"); // Fallback for testing
        }

        try {
            Product createdProduct = productService.createProduct(productRequest, productRequest.getSellerUsername());
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while creating the product: " + e.getMessage());
            e.printStackTrace(); // Log stack trace for server-side debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /api/products/{id} - Get a single product by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable(name = "id") Long productId) { // Explicitly named "id"
        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Product not found with id: " + productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // GET /api/products - Get all products with optional search, pagination, and sorting
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(name = "search", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "6") int pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "createdAt,desc") String sortOrder
    ) {
        try {
            String[] sortParams = sortOrder.split(",");
            // Ensure sortParams has at least one element before accessing parts[0] or parts[1]
            // Basic validation for sortParams can be added here if needed
            Page<Product> products = productService.getAllProducts(searchQuery, pageNumber, pageSize, sortParams);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Avoid sending error details in build() for 500
        }
    }

    // PUT /api/products/{id} - Update an existing product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable(name = "id") Long productId, @RequestBody Product productDetails) { // Explicitly named "id"
        try {
            Product updatedProduct = productService.updateProduct(productId, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) { 
             Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) { // Added null check for e.getMessage()
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while updating the product.");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // DELETE /api/products/{id} - Delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable(name = "id") Long productId) { // Explicitly named "id"
        try {
            productService.deleteProduct(productId);
            // Return a simple success message or no content
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Product with id " + productId + " deleted successfully.");
            return ResponseEntity.ok(successResponse); 
            // Or return ResponseEntity.noContent().build(); for a 204 response
        } catch (RuntimeException e) {
             Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
             if (e.getMessage() != null && e.getMessage().contains("not found")) { // Added null check
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while deleting the product.");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}