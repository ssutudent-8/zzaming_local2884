package com.example.c20232884.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String commentId;   // 댓글 식별 ID
    private String authorUid;   // 댓글 작성자 UID
    private String authorName;  // 댓글 작성자 이름/닉네임
    private String content;     // 댓글 내용
    private String createdAt;   // 작성 시간

    public Comment() {
    }

    public Comment(String commentId, String authorUid, String authorName, String content, String createdAt) {
        this.commentId = commentId;
        this.authorUid = authorUid;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    //Getter
    public String getCommentId() {
        return commentId;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    //Setter
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
