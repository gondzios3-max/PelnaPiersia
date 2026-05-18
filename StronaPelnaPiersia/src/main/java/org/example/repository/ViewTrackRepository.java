package org.example.repository;

import org.example.model.ViewTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViewTrackRepository extends JpaRepository<ViewTrack, Long> {
    Optional<ViewTrack> findByIpAddressAndPostId(String ipAddress, Long postId);
    List<ViewTrack> findByLastViewedAfter(LocalDateTime dateTime);
}
