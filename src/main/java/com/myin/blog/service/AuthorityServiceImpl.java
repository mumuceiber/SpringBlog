package com.myin.blog.service;

import com.myin.blog.domain.Authority;
import com.myin.blog.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityServiceImpl implements AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public Authority getAuthorityById(Long id) {
//        if (authorityRepository.findById(id).isPresent()) {
//            System.out.println(authorityRepository.findById(id).get().getAuthority());
//        } else {
//            System.out.println("cannot find authority");
//        }
        return authorityRepository.findById(id).get();
    }
}
