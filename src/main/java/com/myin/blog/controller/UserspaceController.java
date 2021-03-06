package com.myin.blog.controller;

import com.myin.blog.domain.Blog;
import com.myin.blog.domain.Catalog;
import com.myin.blog.domain.User;
import com.myin.blog.domain.Vote;
import com.myin.blog.service.BlogService;
import com.myin.blog.service.CatalogService;
import com.myin.blog.service.UserService;
import com.myin.blog.util.ConstraintViolationExceptionHandler;
import com.myin.blog.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolationException;
import java.util.List;

@Controller
@RequestMapping("/u")
public class UserspaceController {

    @Autowired
    private UserService userService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CatalogService catalogService;

    @Value("${file.server.url}")
    private String fileServerUrl;

    @GetMapping("{username}")
    public String userSpace(@PathVariable("username") String username, Model model) {
        User user = (User)userDetailsService.loadUserByUsername(username);
        model.addAttribute("user", user);
        return "redirect:/u/" + username + "/blogs";
    }

    @GetMapping("/{username}/profile")
    @PreAuthorize("authentication.name.equals(#username)")
    public ModelAndView profile(@PathVariable("username") String username, Model model) {
        User user = (User)userDetailsService.loadUserByUsername(username);
        model.addAttribute("user", user);
        return new ModelAndView("userspace/profile", "userModel", model);
    }

    @PostMapping("/{username}/profile")
    @PreAuthorize("authentication.name.equals(#username)")
    public String saveProfile(@PathVariable("username") String username, User user) {
        User originalUser = userService.getUserById(user.getId());
        originalUser.setEmail(user.getEmail());
        originalUser.setName(user.getName());

        // determin if the password got changed
        String rawPassword = originalUser.getPassword();
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodePassword = encoder.encode(user.getPassword());
        boolean isMatch = encoder.matches(rawPassword, encodePassword);
        if (!isMatch) {
            originalUser.setEncodePassword(user.getPassword());
        }
        userService.saveOrUpdateUser(originalUser);
        return "redirect:/u/" + username + "/profile";
    }

    @GetMapping("/{username}/avatar")
    @PreAuthorize("authentication.name.equals(#username)")
    public ModelAndView avatar(@PathVariable("username") String username, Model model) {
        User user = (User)userDetailsService.loadUserByUsername(username);
        model.addAttribute("user", user);
        return new ModelAndView("userspace/avatar", "userModel", model);
    }

    @PostMapping("/{username}/avatar")
    @PreAuthorize("authentication.name.equals(#username)")
    public ResponseEntity<Response> saveAvatar(@PathVariable("username") String username, @RequestBody User user) {
        String avatarUrl = user.getAvatar();

        User originalUser = userService.getUserById(user.getId());
        originalUser.setAvatar(avatarUrl);
        userService.saveOrUpdateUser(originalUser);

        return ResponseEntity.ok().body(new Response(true, "success", avatarUrl));
    }

    @GetMapping("/{username}/blogs")
    public String listBlogByOrder(@PathVariable("username") String username,
                                  @RequestParam(value="order", required = false, defaultValue = "new") String order,
                                  @RequestParam(value="catalog", required = false) Long catalogId,
                                  @RequestParam(value="keyword", required = false, defaultValue="") String keyword,
                                  @RequestParam(value="async", required = false) boolean async,
                                  @RequestParam(value="pageIndex", required = false, defaultValue = "0") int pageIndex,
                                  @RequestParam(value="pageSize", required = false, defaultValue = "10") int pageSize,
                                  Model model) {

        User user = (User)userDetailsService.loadUserByUsername(username);
        model.addAttribute("user", user);
        Page<Blog> page = null;

        if (catalogId != null) {
            Catalog catalog = catalogService.getCatalogById(catalogId);
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            page = blogService.listBlogsByCatalog(catalog, pageable);
            order = "";
        } else if (order.equals("hot")) {
            Sort sort = new Sort(Sort.Direction.DESC, "reading", "comments", "like");
            Pageable pageable = new PageRequest(pageIndex, pageSize, sort);
            page = blogService.listBlogsByTitleLikeAndSort(user, keyword, pageable);
        } else if (order.equals("new")) {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            page = blogService.listBlogsByTitleLike(user, keyword, pageable);
        }

        List<Blog> list = page.getContent();
        System.out.println("+++++++++++++++++++++++++++" + list.size());

        model.addAttribute("order", order);
        model.addAttribute("catalogId", catalogId);
        model.addAttribute("page", page);
        model.addAttribute("blogList", list);
        return (async == true ? "userspace/u :: #mainContainerRepleace" : "userspace/u");
    }

    @GetMapping("/{username}/blogs/{id}")
    public String listBlogsByOrder(@PathVariable("username") String username,
                                   @PathVariable("id") Long id, Model model) {

        Blog blog = blogService.getBlogById(id);
        User principal = null;
        // increase reading size each receiving request
        blogService.readingIncrease(id);

        boolean isBlogOwner = false;

        // verify if the executor is the blog owner
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymouseUser")) {

            principal = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null && username.equals(principal.getName())) {
                isBlogOwner = true;
            }
        }

        List<Vote> votes = blog.getVotes();
        Vote currentVote = null;
        if (principal != null) {
            for (Vote vote : votes) {
                if (vote.getUser().getId() == principal.getId()) {
                    currentVote = vote;
                    break;
                }
            }
        }

        model.addAttribute("currentVote", currentVote);
        model.addAttribute("isBlogOwner", isBlogOwner);
        model.addAttribute("blogModel", blogService.getBlogById(id));

        return "userspace/blog";
    }

    @DeleteMapping("/{username}/blogs/{id}")
    @PreAuthorize("authentication.name.equals(#username)")
    public ResponseEntity<Response> deleteBlot(@PathVariable("username") String username,
                                               @PathVariable("id") Long id) {
        try {
            blogService.removeBlog(id);
        } catch (Exception e) {
            return ResponseEntity.ok().body(new Response(false, e.getMessage()));
        }

        String redirectUrl = "/u/" + username + "/blogs";
        return ResponseEntity.ok().body(new Response(true, "delete success", redirectUrl));
    }

    @GetMapping("/{username}/blogs/edit")
    public ModelAndView createBlog(@PathVariable("username") String username, Model model) {
        User user = (User) userDetailsService.loadUserByUsername(username);
        List<Catalog> catalogs = catalogService.listCatalogs(user);

        model.addAttribute("blog", new Blog(null, null, null));
        model.addAttribute("catalogs", catalogs);
        return new ModelAndView("userspace/blogedit", "blogModel", model);
    }

    @GetMapping("/{username}/blogs/edit/{id}")
    public ModelAndView editBlog(@PathVariable("username") String username,@PathVariable("id") Long id, Model model) {
        User user = (User)userDetailsService.loadUserByUsername(username);
        List<Catalog> catalogs = catalogService.listCatalogs(user);

        model.addAttribute("blog", blogService.getBlogById(id));
        model.addAttribute("catalogs", catalogs);
        return new ModelAndView("userspace/blogedit", "blogModel", model);
    }

    @PostMapping("/{username}/blogs/edit")
    @PreAuthorize("authentication.name.equals(#username)")
    public ResponseEntity<Response> saveBlog(@PathVariable("username") String username, @RequestBody Blog blog) {
        // check if catalog is empty
        if (blog.getCatalog() == null || blog.getCatalog().getId() == null) {
            return ResponseEntity.ok().body(new Response(false, "no category"));
        }

        try {
            // check it is for create blog or for edit existing blog
            if (blog.getId() != null) {
                Blog originalBlog = blogService.getBlogById(blog.getId());
                originalBlog.setTitle(blog.getTitle());
                originalBlog.setContent(blog.getContent());
                originalBlog.setSummary(blog.getSummary());
                originalBlog.setTags(blog.getTags());
                originalBlog.setCatalog(blog.getCatalog());
                blogService.saveBlog(originalBlog);
            } else {
                System.out.println("++++++++++++++++++ " + blog.getTags());
                User user = (User)userDetailsService.loadUserByUsername(username);
                blog.setUser(user);
                blogService.saveBlog(blog);
            }
        } catch (ConstraintViolationException e) {
            return ResponseEntity.ok().body(new Response(false, ConstraintViolationExceptionHandler.getMessage(e)));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new Response(false, e.getMessage()));
        }

        String redirectUrl = "/u/" + username + "/blogs/" + blog.getId();
        return ResponseEntity.ok().body(new Response(true, "Save Success", redirectUrl));
    }
}
