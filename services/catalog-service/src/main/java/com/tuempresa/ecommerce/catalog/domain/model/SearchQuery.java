package com.tuempresa.ecommerce.catalog.domain.model;

import java.time.LocalDateTime;

public class SearchQuery {

    private Long id;
    private String term;
    private int page;
    private int size;
    private LocalDateTime createdAt;

    public SearchQuery() {
    }

    public SearchQuery(Long id, String term, int page, int size, LocalDateTime createdAt) {
        this.id = id;
        this.term = term;
        this.page = page;
        this.size = size;
        this.createdAt = createdAt;
    }

    public SearchQuery(String term, int page, int size) {
        this.term = term;
        this.page = page;
        this.size = size;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


