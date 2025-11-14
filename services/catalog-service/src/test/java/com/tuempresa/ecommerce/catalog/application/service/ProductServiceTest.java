package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.catalog.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import com.tuempresa.ecommerce.catalog.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;

    @Mock
    private SearchQueryLoggingService searchQueryLoggingService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private CatalogSettings catalogSettings;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1L, "Test Product", "Test Description", 
                                  BigDecimal.valueOf(19.99), 100, false, 
                                  LocalDateTime.now(), LocalDateTime.now());
        catalogSettings = new CatalogSettings(1L, 10);
    }

    @Test
    @DisplayName("Should return paginated products when findAll is called")
    void shouldReturnPaginatedProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        PaginatedResult<Product> paginatedResult = new PaginatedResult<>(products, 0, 10, 1, 1, true);
        when(catalogSettingsRepositoryPort.findSettings()).thenReturn(Optional.of(catalogSettings));
        when(productRepositoryPort.findAll(0, 10, null, 10)).thenReturn(paginatedResult);

        // When
        PaginatedResult<Product> result = productService.findAll(0, 10, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(catalogSettingsRepositoryPort, times(1)).findSettings();
        verify(productRepositoryPort, times(1)).findAll(0, 10, null, 10);
        verify(searchQueryLoggingService, times(1)).logSearch(any());
    }

    @Test
    @DisplayName("Should return product when findById is called with valid id")
    void shouldReturnProductById() {
        // Given
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Product");
        verify(productRepositoryPort, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when findById is called with non-existent id")
    void shouldReturnEmptyWhenProductNotFound() {
        // Given
        when(productRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(productRepositoryPort, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create product successfully when name is unique")
    void shouldCreateProductSuccessfully() {
        // Given
        Product newProduct = new Product("New Product", "New Description", 
                                        BigDecimal.valueOf(29.99), 50);
        when(productRepositoryPort.findByName("New Product")).thenReturn(Optional.empty());
        when(productRepositoryPort.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.create(newProduct);

        // Then
        assertThat(result).isNotNull();
        verify(productRepositoryPort, times(1)).findByName("New Product");
        verify(productRepositoryPort, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when product name already exists")
    void shouldThrowExceptionWhenProductNameExists() {
        // Given
        Product newProduct = new Product("Existing Product", "Description", 
                                        BigDecimal.valueOf(19.99), 50);
        when(productRepositoryPort.findByName("Existing Product")).thenReturn(Optional.of(testProduct));

        // When/Then
        assertThatThrownBy(() -> productService.create(newProduct))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un producto con el nombre");
        verify(productRepositoryPort, times(1)).findByName("Existing Product");
        verify(productRepositoryPort, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully when valid data is provided")
    void shouldUpdateProductSuccessfully() {
        // Given
        Product updatedProduct = new Product("Updated Product", "Updated Description", 
                                            BigDecimal.valueOf(39.99), 75);
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepositoryPort.existsByNameExcludingId(anyString(), anyLong())).thenReturn(false);
        when(productRepositoryPort.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.update(1L, updatedProduct);

        // Then
        assertThat(result).isNotNull();
        verify(productRepositoryPort, times(1)).findById(1L);
        verify(productRepositoryPort, times(1)).existsByNameExcludingId("Updated Product", 1L);
        verify(productRepositoryPort, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        // Given
        Product updatedProduct = new Product("Updated Product", "Description", 
                                            BigDecimal.valueOf(19.99), 50);
        when(productRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.update(999L, updatedProduct))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
        verify(productRepositoryPort, times(1)).findById(999L);
        verify(productRepositoryPort, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully when product exists")
    void shouldDeleteProductSuccessfully() {
        // Given
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepositoryPort).softDelete(1L);

        // When
        productService.delete(1L);

        // Then
        verify(productRepositoryPort, times(1)).findById(1L);
        verify(productRepositoryPort, times(1)).softDelete(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        // Given
        when(productRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
        verify(productRepositoryPort, times(1)).findById(999L);
        verify(productRepositoryPort, never()).softDelete(anyLong());
    }

    @Test
    @DisplayName("Should sanitize page and size parameters")
    void shouldSanitizePageAndSizeParameters() {
        // Given
        PaginatedResult<Product> paginatedResult = new PaginatedResult<>(Arrays.asList(testProduct), 0, 10, 1, 1, true);
        when(catalogSettingsRepositoryPort.findSettings()).thenReturn(Optional.of(catalogSettings));
        when(productRepositoryPort.findAll(0, 10, null, 10)).thenReturn(paginatedResult);

        // When
        productService.findAll(-1, 0, null);

        // Then
        verify(productRepositoryPort, times(1)).findAll(0, 10, null, 10);
    }
}

