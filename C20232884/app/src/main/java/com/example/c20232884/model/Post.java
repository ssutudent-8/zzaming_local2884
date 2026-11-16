package com.example.c20232884.model;
import java.util.ArrayList;
import java.util.List;

public class Post {
    private String postId;           // 게시글 고유 ID
    private String title;            // 게시글 제목
    private String content;          // 게시글 본문 내용
    private String authorUid;        // 작성자 UID (유저 구분용)
    private String authorName;       // 작성자 이름/닉네임
    private String createdAt;        // 작성 일시
    private List<Comment> comments;  // 댓글 목록 (List 인터페이스 사용)

    // 기본 생성자
    public Post() {
        this.comments = new ArrayList<>();
    }

    // 전체 필드 초기화 생성자
    public Post(String postId, String title, String content, String authorUid, String authorName, String createdAt, List<Comment> comments) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.authorUid = authorUid;
        this.authorName = authorName;
        this.createdAt = createdAt;
        // 넘겨받은 comments가 null일 경우를 대비한 널 체크 안전 코드
        this.comments = (comments != null) ? comments : new ArrayList<>();
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

}