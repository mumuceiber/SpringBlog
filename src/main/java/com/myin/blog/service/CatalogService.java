package com.myin.blog.service;

import com.myin.blog.domain.Catalog;
import com.myin.blog.domain.User;

import java.util.List;

public interface CatalogService {
    Catalog saveCatalog(Catalog catalog);

    void removeCatalog(Long id);

    Catalog getCatalogById(Long id);

    List<Catalog> listCatalogs(User user);
}
