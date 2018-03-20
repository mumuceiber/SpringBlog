package com.myin.blog.service;

import com.myin.blog.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User getUserById(Long id);

    User saveOrUpdateUser(User user);

    User registerUser(User user);

    void removeUser(Long id);

    Page<User> listUsersByNameLike(String name, Pageable pageable);
}
