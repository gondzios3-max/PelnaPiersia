package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ViewTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ipAddress;
    private Long postId;
    private LocalDateTime lastViewed;

    @PrePersist
    protected void onCreate() {
        if (lastViewed == null) {
            lastViewed = LocalDateTime.now();
        }
    }
}
