package com.myin.blog.controller;

import com.myin.blog.domain.Catalog;
import com.myin.blog.domain.User;
import com.myin.blog.service.CatalogService;
import com.myin.blog.service.UserDetailsServiceImpl;
import com.myin.blog.util.ConstraintViolationExceptionHandler;
import com.myin.blog.vo.CatalogVO;
import com.myin.blog.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.util.List;

@Controller
@RequestMapping("/catalogs")
public class CatalogController {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CatalogService catalogService;

    @GetMapping
    public String listCatalogs(@RequestParam(value="username", required = true) String userame, Model model) {
        User user = (User)userDetailsService.loadUserByUsername(userame);
        List<Catalog> catalogs = catalogService.listCatalogs(user);

        // verify if the executor is the owner of the catalog
        boolean isOwner = false;
        if (SecurityContextHolder.getContext().getAuthentication() !=null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                &&  !SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            User principal = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal !=null && user.getUsername().equals(principal.getUsername())) {
                isOwner = true;
            }
        }
        model.addAttribute("isCatalogOwner", isOwner);
        model.addAttribute("catalogs", catalogs);
        return "userspace/u :: #catalogRepleace";
    }

    @PostMapping
    @PreAuthorize("authentication.name.equals(#catalogVO.username)")
    public ResponseEntity<Response> create(@RequestBody CatalogVO catalogVO) {
        String username = catalogVO.getUsername();
        Catalog catalog = catalogVO.getCatalog();

        User user = (User)userDetailsService.loadUserByUsername(username);

        try {
            catalog.setUser(user);
            catalogService.saveCatalog(catalog);
        } catch (ConstraintViolationException e){
            return ResponseEntity.ok().body(new Response(false, ConstraintViolationExceptionHandler.getMessage(e)));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new Response(false, e.getMessage()));
        }
        return ResponseEntity.ok().body(new Response(true, "sccess", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("authentication.name.equals(#username)")
    public ResponseEntity<Response> delete(String username, @PathVariable("id") Long id){
        try {
            catalogService.removeCatalog(id);
        } catch (ConstraintViolationException e) {
            return ResponseEntity.ok().body(new Response(false, ConstraintViolationExceptionHandler.getMessage(e)));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new Response(false, e.getMessage()));
        }
        return ResponseEntity.ok().body(new Response(true, "success", null));
    }

    @GetMapping("/edit")
    public String getCatalogEdit(Model model){
        Catalog catalog = new Catalog(null, null);
        model.addAttribute("catalog", catalog);
        return "userspace/catalogedit";
    }

    @GetMapping("/edit/{id}")
    public String getCatalogById(@PathVariable("id") Long id, Model model) {
        Catalog catalog = catalogService.getCatalogById(id);
        model.addAttribute("catalog", catalog);
        return "userspace/catalogedit";
    }
}
