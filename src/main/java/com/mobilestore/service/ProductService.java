package com.mobilestore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.mobilestore.entity.Product;
import com.mobilestore.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepo;

    // 1. Naya Product Save ya Update karte waqt cache saaf karo
    @CacheEvict(value = {"products", "latest_products", "category_products"}, allEntries = true)
    public void saveProduct(Product p) {
        productRepo.save(p);
    }

    // 2. Product Delete karte waqt bhi cache saaf karo
    @CacheEvict(value = {"products", "latest_products", "category_products"}, allEntries = true)
    public void deleteProduct(Long id) {
        productRepo.deleteById(id);
    }
}