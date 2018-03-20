package com.myin.blog.controller;

import com.myin.blog.domain.Authority;
import com.myin.blog.domain.User;
import com.myin.blog.service.AuthorityService;
import com.myin.blog.service.UserService;
import com.myin.blog.util.ConstraintViolationExceptionHandler;
import com.myin.blog.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorityService authorityService;

    /**
     * Get list of all users
     * @param model
     * @return
     */
    @GetMapping
    public ModelAndView list(@RequestParam(value="async", required = false) boolean async,
                             @RequestParam(value="pageIndex", required = false, defaultValue = "0") int pageIndex,
                             @RequestParam(value="pageSize", required = false, defaultValue = "10") int pageSize,
                             @RequestParam(value="name", required = false, defaultValue = "") String name,
                             Model model) {

        System.out.println("+++++++++++++++= list user");
        Pageable pageable = new PageRequest(pageIndex, pageSize);
        Page<User> page = userService.listUsersByNameLike(name, pageable);
        List<User> list = page.getContent();

        for(User u : list) {
            System.out.println(u.toString());
        }

        model.addAttribute("userList", list);
        model.addAttribute("page", page);
        return new ModelAndView(async == true ? "users/list::#mainContainerRepleace" : "users/list", "userModel", model);

    }

    /**
     * Get create user form page
     * @param model
     * @return
     */
    @GetMapping("/add")
    public ModelAndView createForm(Model model) {
        model.addAttribute("user", new User(null, null, null, null));
        return new ModelAndView("users/add", "userModel", model);
    }

    /**
     * Save or update user
     * @param user
     * @return
     */
    @PostMapping
    public ResponseEntity<Response> saveOrUpdateUser(User user, Long authorityId) {
        List<Authority> authorities = new ArrayList<>();
        System.out.println("Authority ID: " + authorityId);
        authorities.add(authorityService.getAuthorityById(authorityId));
        user.setAuthorities(authorities);

        try {
            userService.saveOrUpdateUser(user);
        } catch (ConstraintViolationException e) {
            System.out.println(ConstraintViolationExceptionHandler.getMessage(e));

            return ResponseEntity.ok().body(new Response(false, ConstraintViolationExceptionHandler.getMessage(e)));
        }
        return ResponseEntity.ok().body(new Response(true, "Success", user));
    }

    /**
     * Delete User by Id
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteUser(@PathVariable("id") Long id, Model model) {
        try {
            userService.removeUser(id);
        } catch (Exception e) {
            return ResponseEntity.ok().body(new Response(false, e.getMessage()));
        }
        return ResponseEntity.ok().body(new Response(true, "Success"));
    }

    @GetMapping("/edit/{id}")
    public ModelAndView modifyUser(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return new ModelAndView("users/edit", "userModel", model);
    }

}
