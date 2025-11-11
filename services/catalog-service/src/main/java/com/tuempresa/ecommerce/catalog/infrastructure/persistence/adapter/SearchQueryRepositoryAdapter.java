package com.tuempresa.ecommerce.catalog.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.domain.port.out.SearchQueryRepositoryPort;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.SearchQueryEntity;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.mapper.SearchQueryEntityMapper;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository.SearchQueryJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class SearchQueryRepositoryAdapter implements SearchQueryRepositoryPort {

    private final SearchQueryJpaRepository searchQueryJpaRepository;

    public SearchQueryRepositoryAdapter(SearchQueryJpaRepository searchQueryJpaRepository) {
        this.searchQueryJpaRepository = searchQueryJpaRepository;
    }

    @Override
    public SearchQuery save(SearchQuery searchQuery) {
        SearchQueryEntity entity = SearchQueryEntityMapper.toEntity(searchQuery);
        SearchQueryEntity saved = searchQueryJpaRepository.save(entity);
        return SearchQueryEntityMapper.toDomain(saved);
    }
}


