package com.tuempresa.ecommerce.catalog.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.SearchQueryEntity;

public class SearchQueryEntityMapper {

    private SearchQueryEntityMapper() {
    }

    public static SearchQuery toDomain(SearchQueryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SearchQuery(
                entity.getId(),
                entity.getTerm(),
                entity.getPage(),
                entity.getSize(),
                entity.getCreatedAt()
        );
    }

    public static SearchQueryEntity toEntity(SearchQuery searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        return new SearchQueryEntity(
                searchQuery.getId(),
                searchQuery.getTerm(),
                searchQuery.getPage(),
                searchQuery.getSize(),
                searchQuery.getCreatedAt()
        );
    }
}


