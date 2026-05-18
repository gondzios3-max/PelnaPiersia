package org.example.repository;

import org.example.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthorEmailAndCreatedAtAfter(String authorEmail, LocalDateTime dateTime);
    List<Comment> findByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime dateTime);
    List<Comment> findByCreatedAtAfter(LocalDateTime dateTime);
}
