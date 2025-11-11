package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.domain.port.out.SearchQueryRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchQueryLoggingServiceTest {

    @Mock
    private SearchQueryRepositoryPort searchQueryRepositoryPort;

    @InjectMocks
    private SearchQueryLoggingService searchQueryLoggingService;

    @Test
    void logSearch_persistsQuery_whenPresent() {
        SearchQuery searchQuery = new SearchQuery("laptop", 1, 10);

        searchQueryLoggingService.logSearch(searchQuery);

        verify(searchQueryRepositoryPort).save(searchQuery);
    }

    @Test
    void logSearch_doesNothing_whenQueryNull() {
        searchQueryLoggingService.logSearch(null);

        verify(searchQueryRepositoryPort, never()).save(null);
    }
}

