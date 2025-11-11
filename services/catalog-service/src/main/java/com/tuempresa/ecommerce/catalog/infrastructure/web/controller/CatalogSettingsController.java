package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.in.CatalogSettingsUseCase;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.mapper.CatalogSettingsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog/settings")
public class CatalogSettingsController {

    private final CatalogSettingsUseCase catalogSettingsUseCase;

    public CatalogSettingsController(CatalogSettingsUseCase catalogSettingsUseCase) {
        this.catalogSettingsUseCase = catalogSettingsUseCase;
    }

    @GetMapping
    public ResponseEntity<CatalogSettingsResponse> getSettings() {
        CatalogSettings settings = catalogSettingsUseCase.getSettings();
        return ResponseEntity.status(HttpStatus.OK).body(CatalogSettingsWebMapper.toResponse(settings));
    }

    @PutMapping
    public ResponseEntity<CatalogSettingsResponse> updateSettings(@Valid @RequestBody CatalogSettingsRequest request) {
        CatalogSettings currentSettings = catalogSettingsUseCase.getSettings();
        CatalogSettings toUpdate = CatalogSettingsWebMapper.toDomain(request, currentSettings.getId());
        CatalogSettings updated = catalogSettingsUseCase.updateSettings(toUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(CatalogSettingsWebMapper.toResponse(updated));
    }
}


