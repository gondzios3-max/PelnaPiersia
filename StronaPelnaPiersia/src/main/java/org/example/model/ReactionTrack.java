package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ReactionTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ipAddress;
    private Long postId;
    private LocalDateTime reactedAt;

    @PrePersist
    protected void onCreate() {
        if (reactedAt == null) {
            reactedAt = LocalDateTime.now();
        }
    }
}
