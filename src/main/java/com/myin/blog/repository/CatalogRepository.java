package com.myin.blog.repository;

import com.myin.blog.domain.Catalog;
import com.myin.blog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, Long>{

    List<Catalog> findByUser(User user);

    List<Catalog> findByUserAndName(User user, String name);
}
