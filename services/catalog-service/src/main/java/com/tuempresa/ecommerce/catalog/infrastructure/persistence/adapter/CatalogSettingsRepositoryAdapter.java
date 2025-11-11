package com.tuempresa.ecommerce.catalog.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.CatalogSettingsEntity;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.mapper.CatalogSettingsEntityMapper;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository.CatalogSettingsJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CatalogSettingsRepositoryAdapter implements CatalogSettingsRepositoryPort {

    private final CatalogSettingsJpaRepository catalogSettingsJpaRepository;

    public CatalogSettingsRepositoryAdapter(CatalogSettingsJpaRepository catalogSettingsJpaRepository) {
        this.catalogSettingsJpaRepository = catalogSettingsJpaRepository;
    }

    @Override
    public Optional<CatalogSettings> findSettings() {
        return catalogSettingsJpaRepository.findTopByOrderByIdAsc()
                .map(CatalogSettingsEntityMapper::toDomain);
    }

    @Override
    public CatalogSettings save(CatalogSettings settings) {
        CatalogSettingsEntity entity = CatalogSettingsEntityMapper.toEntity(settings);
        CatalogSettingsEntity saved = catalogSettingsJpaRepository.save(entity);
        return CatalogSettingsEntityMapper.toDomain(saved);
    }
}


