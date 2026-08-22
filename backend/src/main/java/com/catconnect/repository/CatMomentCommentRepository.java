package com.catconnect.repository;

import com.catconnect.entity.CatMomentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CatMomentCommentRepository extends JpaRepository<CatMomentComment, Long> {
    List<CatMomentComment> findByMomentIdOrderByCreatedAtAsc(Long momentId);
}
