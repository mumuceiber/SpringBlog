package com.myin.blog.controller;

import com.myin.blog.vo.Menu;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admins")
public class AdminController {

    @GetMapping
    public ModelAndView listUsers(Model model) {
        List<Menu> list = new ArrayList<>();
        list.add(new Menu("User Management", "/users"));
        list.add(new Menu("Role Management", "/roles"));
        list.add(new Menu("Blog Management", "/blogs"));
        list.add(new Menu("Comment Management", "/commits"));
        model.addAttribute("list", list);
        return new ModelAndView("admins/index", "model", model);
    }

}
