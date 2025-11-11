package com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.SearchQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryJpaRepository extends JpaRepository<SearchQueryEntity, Long> {
}


