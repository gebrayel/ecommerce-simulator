package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.domain.port.out.SearchQueryRepositoryPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SearchQueryLoggingService {

    private final SearchQueryRepositoryPort searchQueryRepositoryPort;

    public SearchQueryLoggingService(SearchQueryRepositoryPort searchQueryRepositoryPort) {
        this.searchQueryRepositoryPort = searchQueryRepositoryPort;
    }

    @Async
    public void logSearch(SearchQuery searchQuery) {
        if (searchQuery != null) {
            searchQueryRepositoryPort.save(searchQuery);
        }
    }
}


