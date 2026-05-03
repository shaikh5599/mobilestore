package com.mobilestore.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import com.mobilestore.entity.StoreSetting;

public interface StoreRepository extends JpaRepository<StoreSetting, Long> {

    // Store settings by ID bhi cache hongi
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<StoreSetting> findById(Long id);
}