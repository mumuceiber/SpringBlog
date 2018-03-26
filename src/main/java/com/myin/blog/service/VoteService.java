package com.myin.blog.service;

import com.myin.blog.domain.Vote;

public interface VoteService {

    Vote getVoteById(Long id);

    void removeVote(Long id);
}
