package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.catalog.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import com.tuempresa.ecommerce.catalog.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;
    @Mock
    private SearchQueryLoggingService searchQueryLoggingService;
    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        when(catalogSettingsRepositoryPort.findSettings())
                .thenReturn(Optional.empty());
    }

    @Test
    void findAll_sanitizesParameters_andLogsSearchQuery() {
        PaginatedResult<Product> expectedResult = new PaginatedResult<>(List.of(), 0, 25, 0, 0, true);
        when(productRepositoryPort.findAll(eq(0), eq(25), eq("laptop"), eq(0)))
                .thenReturn(expectedResult);

        PaginatedResult<Product> result = productService.findAll(-2, 25, "  laptop  ");

        assertThat(result).isSameAs(expectedResult);
        ArgumentCaptor<SearchQuery> searchQueryCaptor = ArgumentCaptor.forClass(SearchQuery.class);
        verify(searchQueryLoggingService).logSearch(searchQueryCaptor.capture());
        SearchQuery capturedQuery = searchQueryCaptor.getValue();
        assertThat(capturedQuery.getTerm()).isEqualTo("laptop");
        assertThat(capturedQuery.getPage()).isZero();
        assertThat(capturedQuery.getSize()).isEqualTo(25);
    }

    @Test
    void create_setsDefaultFieldsAndPersists_whenNameIsUnique() {
        Product incoming = new Product();
        incoming.setName("Laptop");
        incoming.setDescription("High-end");
        incoming.setPrice(BigDecimal.valueOf(1500));
        incoming.setStock(5);

        when(productRepositoryPort.findByName("Laptop")).thenReturn(Optional.empty());
        when(productRepositoryPort.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Product.class));

        Product saved = productService.create(incoming);

        verify(productRepositoryPort).save(productCaptor.capture());
        Product captured = productCaptor.getValue();
        assertThat(captured.isDeleted()).isFalse();
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getName()).isEqualTo("Laptop");
        assertThat(saved).isSameAs(captured);
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameExists() {
        when(productRepositoryPort.findByName("Laptop"))
                .thenReturn(Optional.of(new Product()));

        Product incoming = new Product();
        incoming.setName("Laptop");

        assertThatThrownBy(() -> productService.create(incoming))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Laptop");

        verify(productRepositoryPort).findByName("Laptop");
        verifyNoMoreInteractions(productRepositoryPort);
    }

    @Test
    void update_replacesFieldsAndPersists_whenValid() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Laptop");
        existing.setDescription("Desc");
        existing.setPrice(BigDecimal.ONE);
        existing.setStock(2);
        existing.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.existsByNameExcludingId("New Laptop", 1L)).thenReturn(false);
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product incoming = new Product();
        incoming.setName("New Laptop");
        incoming.setDescription("New Desc");
        incoming.setPrice(BigDecimal.TEN);
        incoming.setStock(10);

        Product result = productService.update(1L, incoming);

        verify(productRepositoryPort).save(productCaptor.capture());
        Product persisted = productCaptor.getValue();
        assertThat(persisted.getName()).isEqualTo("New Laptop");
        assertThat(persisted.getDescription()).isEqualTo("New Desc");
        assertThat(persisted.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(persisted.getStock()).isEqualTo(10);
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(result).isSameAs(persisted);
    }

    @Test
    void update_throwsResourceNotFound_whenProductMissing() {
        when(productRepositoryPort.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(9L, new Product()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepositoryPort).findById(9L);
        verifyNoMoreInteractions(productRepositoryPort);
    }

    @Test
    void update_throwsDuplicateResource_whenNameExists() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Laptop");

        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.existsByNameExcludingId("New", 1L)).thenReturn(true);

        Product incoming = new Product();
        incoming.setName("New");

        assertThatThrownBy(() -> productService.update(1L, incoming))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_invokesSoftDelete_whenProductExists() {
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(new Product()));

        productService.delete(1L);

        verify(productRepositoryPort).softDelete(1L);
    }

    @Test
    void delete_throwsResourceNotFound_whenProductMissing() {
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepositoryPort).findById(5L);
        verify(productRepositoryPort, times(0)).softDelete(any());
        verifyNoInteractions(searchQueryLoggingService);
    }
}

