package org.example.service;

import org.example.model.Comment;
import org.example.model.Post;
import org.example.model.ViewTrack;
import org.example.model.ReactionTrack;
import org.example.repository.CommentRepository;
import org.example.repository.PostRepository;
import org.example.repository.ViewTrackRepository;
import org.example.repository.ReactionTrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ViewTrackRepository viewTrackRepository;
    
    @Autowired
    private ReactionTrackRepository reactionTrackRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Page<Post> getPaginatedPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post savePost(Post post, MultipartFile imageFile) throws IOException {
        if (post.getId() != null) {
            // Edycja istniejącego posta - musimy zachować komentarze!
            Post existingPost = getPostById(post.getId());
            if (existingPost != null) {
                post.setComments(existingPost.getComments());
                // Jeśli nie przesłano nowego zdjęcia, zachowaj stare
                if (imageFile == null || imageFile.isEmpty()) {
                    post.setImageUrl(existingPost.getImageUrl());
                }
            }
        }

        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath);
            
            post.setImageUrl("/" + uploadPath + "/" + fileName);
        }

        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public void trackView(Long postId, String ipAddress) {
        Optional<ViewTrack> trackOpt = viewTrackRepository.findByIpAddressAndPostId(ipAddress, postId);
        LocalDateTime now = LocalDateTime.now();
        
        if (trackOpt.isEmpty() || trackOpt.get().getLastViewed().isBefore(now.minusHours(1))) {
            Post post = getPostById(postId);
            if (post != null) {
                post.setViewCount(post.getViewCount() + 1);
                postRepository.save(post);
                
                ViewTrack track = trackOpt.orElse(new ViewTrack());
                track.setIpAddress(ipAddress);
                track.setPostId(postId);
                track.setLastViewed(now);
                viewTrackRepository.save(track);
            }
        }
    }

    public void addReaction(Long postId, String type, String ipAddress) {
        Optional<ReactionTrack> trackOpt = reactionTrackRepository.findByIpAddressAndPostId(ipAddress, postId);
        LocalDateTime now = LocalDateTime.now();
        
        // Blokada: jedna reakcja na 24h na IP
        if (trackOpt.isEmpty() || trackOpt.get().getReactedAt().isBefore(now.minusHours(24))) {
            Post post = getPostById(postId);
            if (post != null) {
                switch (type) {
                    case "like" -> post.setLikeCount(post.getLikeCount() + 1);
                    case "heart" -> post.setHeartCount(post.getHeartCount() + 1);
                    case "sad" -> post.setSadCount(post.getSadCount() + 1);
                }
                postRepository.save(post);
                
                ReactionTrack track = trackOpt.orElse(new ReactionTrack());
                track.setIpAddress(ipAddress);
                track.setPostId(postId);
                track.setReactedAt(now);
                reactionTrackRepository.save(track);
            }
        }
    }

    public void addComment(Long postId, Comment comment, String ipAddress) {
        Post post = getPostById(postId);
        if (post != null) {
            LocalDateTime limitDate = LocalDateTime.now().minusHours(24);
            
            // Sprawdzanie limitu: maks 2 komentarze na 24h na IP lub Email
            long countByEmail = commentRepository.findByAuthorEmailAndCreatedAtAfter(comment.getAuthorEmail(), limitDate).size();
            long countByIp = commentRepository.findByIpAddressAndCreatedAtAfter(ipAddress, limitDate).size();
            
            if (countByEmail < 2 && countByIp < 2) {
                comment.setPost(post);
                comment.setIpAddress(ipAddress);
                comment.setCreatedAt(LocalDateTime.now());
                commentRepository.save(comment);
                
                // Opcjonalnie: upewnienie się, że post wie o nowym komentarzu w pamięci
                if (post.getComments() == null) {
                    post.setComments(new java.util.ArrayList<>());
                }
                post.getComments().add(comment);
                postRepository.save(post);
            }
        }
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<Post> searchPosts(String query) {
        return postRepository.findAll().stream()
                .filter(p -> p.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                            p.getContent().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Post> getPostsByCategory(String category) {
        return postRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfLastWeek = LocalDate.now().minusDays(7).atStartOfDay();

        // 1. Unikalni goście dzisiaj
        long uniqueVisitorsToday = viewTrackRepository.findByLastViewedAfter(startOfToday).stream()
                .map(ViewTrack::getIpAddress)
                .distinct()
                .count();
        stats.put("uniqueVisitorsToday", uniqueVisitorsToday);

        // 2. Wszystkie wyświetlenia (suma z postów)
        long totalViews = postRepository.findAll().stream()
                .mapToLong(Post::getViewCount)
                .sum();
        stats.put("totalViews", totalViews);

        // 3. Komentarze w tym tygodniu
        long commentsThisWeek = commentRepository.findByCreatedAtAfter(startOfLastWeek).size();
        stats.put("commentsThisWeek", commentsThisWeek);

        // 4. Suma reakcji (wszystkie typy)
        long totalReactions = postRepository.findAll().stream()
                .mapToLong(p -> p.getLikeCount() + p.getHeartCount() + p.getSadCount())
                .sum();
        stats.put("totalReactions", totalReactions);

        // 5. Liczba postów
        stats.put("totalPosts", postRepository.count());

        // 6. Ostatnia aktywność (najnowsze komentarze)
        stats.put("latestComments", commentRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList()));

        return stats;
    }
}
