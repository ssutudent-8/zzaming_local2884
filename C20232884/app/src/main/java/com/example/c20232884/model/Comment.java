package com.example.c20232884.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;          // 댓글 고유 ID (id)
    private String postId;      // 연관 게시글 고유 ID
    private String authorUid;   // 댓글 작성자 UID
    private String authorName;  // 댓글 작성자 이름/닉네임
    private String content;     // 댓글 내용
    private String createdAt;   // 작성 시간

    public Comment() {
    }

    public Comment(String id, String postId, String authorUid, String authorName, String content, String createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorUid = authorUid;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Comment(String id, String authorUid, String authorName, String content, String createdAt) {
        this(id, "", authorUid, authorName, content, createdAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
