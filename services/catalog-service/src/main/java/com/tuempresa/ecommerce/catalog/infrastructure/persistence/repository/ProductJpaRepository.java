package com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT p FROM ProductEntity p " +
           "WHERE p.deleted = false AND p.stock >= :minimumStock")
    Page<ProductEntity> findAllByDeletedFalseWithMinimumStock(@Param("minimumStock") int minimumStock, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p " +
           "WHERE p.deleted = false AND p.stock >= :minimumStock AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductEntity> searchByNameOrDescription(@Param("search") String search,
                                                  @Param("minimumStock") int minimumStock,
                                                  Pageable pageable);

    Optional<ProductEntity> findByIdAndDeletedFalse(Long id);

    Optional<ProductEntity> findByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedFalse(String name, Long id);
}


