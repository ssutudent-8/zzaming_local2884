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
    private int likes = 0;
    private String category;         // 카테고리 ("공지", "자유", "질문", "마음의 편지")

    // 기본 생성자
    public Post() {
        this.comments = new ArrayList<>();
        this.category = "자유";
    }

    // 전체 필드 초기화 생성자 (category 호환)
    public Post(String postId, String title, String content, String authorUid, String authorName, String createdAt, List<Comment> comments, String category) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.authorUid = authorUid;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.comments = (comments != null) ? comments : new ArrayList<>();
        this.category = (category != null) ? category : "자유";
    }

    public Post(String postId, String title, String content, String authorUid, String authorName, String createdAt, List<Comment> comments) {
        this(postId, title, content, authorUid, authorName, createdAt, comments, "자유");
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
