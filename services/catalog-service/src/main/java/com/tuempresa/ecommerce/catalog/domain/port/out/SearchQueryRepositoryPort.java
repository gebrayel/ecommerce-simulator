package com.tuempresa.ecommerce.catalog.domain.port.out;

import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;

public interface SearchQueryRepositoryPort {

    SearchQuery save(SearchQuery searchQuery);
}


