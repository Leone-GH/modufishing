package com.fishtripplanner.repository;

import com.fishtripplanner.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 제목 또는 내용에 특정 키워드가 포함된 게시글을 페이징 처리하여 조회
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    // 인기 게시글 상위 9개
    List<Post> findTop9ByOrderByViewCountDesc();

    // 최신 게시글 상위 10개
    List<Post> findTop10ByOrderByCreatedAtDesc();

    // 대소문자 구분 없이 제목 또는 내용에 특정 키워드가 포함된 게시글을 조회
    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);

    // ★ 추가: 인기 게시글 상위 12개
    List<Post> findTop12ByOrderByViewCountDesc();
}
