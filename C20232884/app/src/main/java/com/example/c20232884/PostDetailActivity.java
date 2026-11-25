package com.example.c20232884;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.c20232884.databinding.ActivityPostDetailBinding;
import com.example.c20232884.model.Comment;
import com.example.c20232884.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 본문 상세 조회, 좋아요 및 댓글 실시간 작성을 담당하는 액티비티 클래스
 */
public class PostDetailActivity extends AppCompatActivity {

    private ActivityPostDetailBinding binding;
    private int likeCount = 0;
    private boolean isLiked = false;
    private final List<Comment> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 전달받은 게시글 정보 세팅
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");
        String title = intent.getStringExtra("postTitle");
        String content = intent.getStringExtra("postContent");
        String author = intent.getStringExtra("postAuthor");
        String time = intent.getStringExtra("postTime");

        // 전달받은 기존 댓글 또는 서버 동기화 댓글 수신
        Serializable passedCommentsObj = intent.getSerializableExtra("postComments");
        if (passedCommentsObj instanceof List) {
            commentList.clear();
            commentList.addAll((List<Comment>) passedCommentsObj);
        } else {
            MockServerDataSource.getInstance(this).fetchCommentsForPost(postId, new MockServerDataSource.OnCommentLoadListener() {
                @Override
                public void onSuccess(List<Comment> comments) {
                    commentList.clear();
                    if (comments != null) {
                        commentList.addAll(comments);
                    }
                    binding.tvCommentTotal.setText(String.valueOf(commentList.size()));
                }

                @Override
                public void onFailure(Exception e) {}
            });
        }

        binding.tvPostTitle.setText(title != null ? title : "게시글 제목");
        binding.tvPostBody.setText(content != null ? content : "본문 내용");
        binding.tvAuthorName.setText(author != null ? author : "작성자");
        binding.tvPostTime.setText(time != null ? time : "방금 전");

        if (author != null && !author.isEmpty()) {
            binding.tvAuthorAvatar.setText(author.substring(0, 1));
        }

        // 유저 권한 정보 및 게시글 삭제 권한 (간부 유저 또는 행정반/본인 작성자)
        String postAuthorUid = intent.getStringExtra("postAuthorUid");
        User currentUser = MockServerDataSource.getInstance(this).getCurrentUser();

        boolean isOfficer = "간부".equals(currentUser.getMemberType());
        boolean isMyPost = (postAuthorUid != null && postAuthorUid.equals(currentUser.getId()))
                || currentUser.getUserName().equals(author);
        boolean canDeletePost = isOfficer || isMyPost;

        if (canDeletePost) {
            binding.btnDeletePost.setVisibility(View.VISIBLE);
            binding.btnDeletePost.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("게시글 삭제")
                        .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            MockServerDataSource.getInstance(this).deletePost(postId, new MockServerDataSource.OnCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("postId", postId);
                                    resultIntent.putExtra("isDeleted", true);
                                    setResult(RESULT_OK, resultIntent);
                                    Toast.makeText(PostDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(PostDetailActivity.this, "게시글 삭제 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        } else {
            binding.btnDeletePost.setVisibility(View.GONE);
        }

        // 뒤로가기
        binding.btnBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("postId", postId);
            resultIntent.putExtra("updatedLikeCount", likeCount);
            resultIntent.putExtra("updatedCommentCount", commentList.size());
            resultIntent.putExtra("updatedComments", (Serializable) commentList);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 좋아요 버튼
        binding.btnLike.setText("좋아요 " + likeCount);
        updateLikeButtonUI();

        binding.btnLike.setOnClickListener(v -> {
            if (isLiked) {
                isLiked = false;
                likeCount--;
                Toast.makeText(this, "좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                isLiked = true;
                likeCount++;
                Toast.makeText(this, "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();
            }
            updateLikeButtonUI();
            MockServerDataSource.getInstance(this).updatePostLikes(postId, likeCount, null);
        });

        // 댓글 리사이클러뷰 연결
        CommentAdapter commentAdapter = new CommentAdapter(commentList);

        commentAdapter.setOnCommentDeleteListener((position, comment) -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("댓글 삭제")
                    .setMessage("댓글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        if (position >= 0 && position < commentList.size()) {
                            Comment targetComment = commentList.get(position);
                            commentList.remove(position);
                            commentAdapter.notifyItemRemoved(position);
                            binding.tvCommentTotal.setText(String.valueOf(commentList.size()));
                            MockServerDataSource.getInstance(this).deleteCommentFromPost(postId, targetComment.getId(), null);
                            Toast.makeText(this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        binding.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);
        binding.tvCommentTotal.setText(String.valueOf(commentList.size()));

        // 댓글 등록 버튼
        binding.btnSendComment.setOnClickListener(v -> {
            String commentText = binding.inputComment.getText().toString().trim();
            if (commentText.isEmpty()) {
                binding.inputComment.setError("댓글을 입력해주세요.");
                return;
            }

            Comment newComment = new Comment("c_" + System.currentTimeMillis(), postId, currentUser.getId(), currentUser.getUserName(), commentText, "방금 전");
            commentList.add(newComment);
            commentAdapter.notifyItemInserted(commentList.size() - 1);
            binding.tvCommentTotal.setText(String.valueOf(commentList.size()));
            MockServerDataSource.getInstance(this).addCommentToPost(postId, newComment, null);

            binding.inputComment.setText("");
            Toast.makeText(this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLikeButtonUI() {
        binding.btnLike.setSelected(isLiked);
        binding.btnLike.setText((isLiked ? "◆ 좋아요 " : "◇ 좋아요 ") + likeCount);
        int colorRes = isLiked ? R.color.personal : R.color.text_secondary;
        binding.btnLike.setTextColor(androidx.core.content.ContextCompat.getColor(this, colorRes));
    }
}
