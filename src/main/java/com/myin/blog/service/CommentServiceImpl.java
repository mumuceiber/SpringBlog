package com.myin.blog.service;

import com.myin.blog.domain.Comment;
import com.myin.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService{

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).get();
    }

    @Override
    public void removeComment(Long id) {
        commentRepository.deleteById(id);
    }
}
