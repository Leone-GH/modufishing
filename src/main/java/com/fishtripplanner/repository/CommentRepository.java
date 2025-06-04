package com.fishtripplanner.repository;

import com.fishtripplanner.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    void deleteByPostId(Long postId); // 게시글 삭제 시 댓글도 삭제되게 추가
}
