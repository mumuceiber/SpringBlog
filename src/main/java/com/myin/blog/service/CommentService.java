package com.myin.blog.service;

import com.myin.blog.domain.Comment;

public interface CommentService {

    Comment getCommentById(Long id);

    void removeComment(Long id);
}
