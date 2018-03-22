package com.myin.blog.service;

import com.myin.blog.domain.Blog;
import com.myin.blog.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {

    Blog saveBlog(Blog blog);

    void removeBlog(Long id);

    Blog updateBlog(Blog blog);

    Blog getBlogById(Long id);

    Page<Blog> listBlogsByTitleLike(User user, String title, Pageable pageable);

    Page<Blog> listBlogsByTitleLikeAndSort(User user, String title, Pageable pageable);

    void readingIncrease(Long id);
}
