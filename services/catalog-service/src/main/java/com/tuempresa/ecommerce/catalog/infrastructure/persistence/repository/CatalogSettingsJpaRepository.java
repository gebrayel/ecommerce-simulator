package com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.CatalogSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CatalogSettingsJpaRepository extends JpaRepository<CatalogSettingsEntity, Long> {

    Optional<CatalogSettingsEntity> findTopByOrderByIdAsc();
}


