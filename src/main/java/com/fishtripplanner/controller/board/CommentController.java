package com.fishtripplanner.controller.board;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.domain.comment.Comment;
import com.fishtripplanner.repository.CommentRepository;
import com.fishtripplanner.repository.PostRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @PostMapping("/add")
    public String addComment(@RequestParam Long postId,
                             @RequestParam String writer,
                             @RequestParam String content,
                             HttpSession session) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("로그인된 사용자 정보가 없습니다.");
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .writer(writer)
                .content(content)
                .build();

        commentRepository.save(comment);

        return "redirect:/posts/" + postId;
    }

    @Transactional // 중요: 좋아요 증가가 DB에 반영되도록 보장
    @PostMapping("/like/{commentId}")
    @ResponseBody
    public String likeComment(@PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 없습니다."));
        comment.increaseLikeCount();
        return String.valueOf(comment.getLikeCount());
    }
}
