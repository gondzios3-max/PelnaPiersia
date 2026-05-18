package org.example.repository;

import org.example.model.ReactionTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionTrackRepository extends JpaRepository<ReactionTrack, Long> {
    Optional<ReactionTrack> findByIpAddressAndPostId(String ipAddress, Long postId);
    List<ReactionTrack> findByReactedAtAfter(LocalDateTime dateTime);
}
