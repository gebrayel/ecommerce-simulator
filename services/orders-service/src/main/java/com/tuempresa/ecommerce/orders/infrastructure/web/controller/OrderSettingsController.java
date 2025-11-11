package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.OrderSettingsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders/settings")
public class OrderSettingsController {

    private final OrderSettingsUseCase orderSettingsUseCase;

    public OrderSettingsController(OrderSettingsUseCase orderSettingsUseCase) {
        this.orderSettingsUseCase = orderSettingsUseCase;
    }

    @GetMapping
    public ResponseEntity<OrderSettingsResponse> getSettings() {
        return ResponseEntity.ok(OrderSettingsWebMapper.toResponse(orderSettingsUseCase.getSettings()));
    }

    @PutMapping
    public ResponseEntity<OrderSettingsResponse> updateSettings(@Valid @RequestBody OrderSettingsRequest request) {
        var updated = orderSettingsUseCase.updateSettings(OrderSettingsWebMapper.toDomain(request));
        return ResponseEntity.ok(OrderSettingsWebMapper.toResponse(updated));
    }
}


