package org.example.controller;

import org.example.model.Post;
import org.example.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
public class HomeController {

    @Autowired
    private PostService postService;

    @GetMapping("/")
    public String home(Model model, 
                       @RequestParam(required = false) String search, 
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "0") int page) {
        
        var allPosts = postService.getAllPosts();
        
        // Obliczanie liczby postów w każdej kategorii
        Map<String, Long> categoryCounts = allPosts.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(Post::getCategory, Collectors.counting()));
        
        model.addAttribute("categoryCounts", categoryCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 0); // Domyślnie 0, nadpiszemy jeśli nie szukamy

        if (search != null) {
            model.addAttribute("posts", postService.searchPosts(search));
        } else if (category != null) {
            model.addAttribute("posts", postService.getPostsByCategory(category));
        } else {
            var pageable = PageRequest.of(page, 3, Sort.by("createdAt").descending());
            var postPage = postService.getPaginatedPosts(pageable);
            model.addAttribute("posts", postPage.getContent());
            model.addAttribute("totalPages", postPage.getTotalPages());
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
