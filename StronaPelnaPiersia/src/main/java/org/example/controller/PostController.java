package org.example.controller;

import org.example.model.Comment;
import org.example.model.Post;
import org.example.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.getAllPosts());
        return "posts/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/form";
    }

    @PostMapping
    public String savePost(@ModelAttribute Post post, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            postService.savePost(post, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/posts/new?error";
        }
        return "redirect:/posts";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.getPostById(id));
        return "posts/form";
    }

    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        model.addAllAttributes(postService.getAdminStats());
        return "posts/stats";
    }

    @GetMapping("/view/{id}")
    public String viewPost(@PathVariable Long id, Model model, HttpServletRequest request) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return "redirect:/?error=post_not_found";
        }
        
        String ip = request.getRemoteAddr();
        postService.trackView(id, ip);
        model.addAttribute("post", post);
        model.addAttribute("newComment", new Comment());
        return "posts/view";
    }

    @PostMapping("/comment/{postId}")
    public String addComment(@PathVariable Long postId, @ModelAttribute Comment comment, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        postService.addComment(postId, comment, ip);
        return "redirect:/posts/view/" + postId;
    }

    @GetMapping("/comment/delete/{postId}/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        postService.deleteComment(commentId);
        return "redirect:/posts/view/" + postId;
    }

    @GetMapping("/react/{postId}/{type}")
    public String addReaction(@PathVariable Long postId, @PathVariable String type, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        postService.addReaction(postId, type, ip);
        return "redirect:/posts/view/" + postId;
    }
}
