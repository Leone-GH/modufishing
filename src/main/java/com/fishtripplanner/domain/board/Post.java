package com.fishtripplanner.domain.board;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String writer;

    /**
     * 여러 이미지 경로를 ','로 구분하여 저장
     * ex: /uploads/abc.jpg,/uploads/def.jpg
     */
    private String imagePath;

    private String videoPath;

    private String profileImagePath;

    @Column(nullable = false)
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
