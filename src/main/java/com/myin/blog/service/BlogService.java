package com.myin.blog.service;

import com.myin.blog.domain.Blog;
import com.myin.blog.domain.Catalog;
import com.myin.blog.domain.User;
import com.myin.blog.domain.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {

    Blog saveBlog(Blog blog);

    void removeBlog(Long id);

    Blog updateBlog(Blog blog);

    Blog getBlogById(Long id);

    Page<Blog> listBlogsByTitleVote(User user, String title, Pageable pageable);

    Page<Blog> listBlogsByTitleVoteAndSort(User user, String title, Pageable pageable);

    Page<Blog> listBlogsByTitleLike(User user, String title, Pageable pageable);

    Page<Blog> listBlogsByTitleLikeAndSort(User user, String title, Pageable pageable);

    Page<Blog> listBlogsByCatalog(Catalog catalog, Pageable pageable);

    void readingIncrease(Long id);

    Blog createComment(Long blogId, String commentContent);

    void removeComment(Long blogId, Long commentId);

    Vote createVote(Long blogId);

    void removeVote(Long blogId, Long voteId);
}
