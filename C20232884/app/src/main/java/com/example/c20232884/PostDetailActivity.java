package com.example.c20232884;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.c20232884.databinding.ActivityPostDetailBinding;
import com.example.c20232884.model.Comment;

/**
 * 게시글 본문 상세 조회, 좋아요 및 댓글 실시간 작성을 담당하는 액티비티 클래스
 */
public class PostDetailActivity extends AppCompatActivity {

    private ActivityPostDetailBinding binding;
    private int likeCount = 0;
    private boolean isLiked = false;
    private java.util.List<Comment> commentList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 전달받은 게시글 정보 세팅
        String postId = getIntent().getStringExtra("postId");
        String title = getIntent().getStringExtra("postTitle");
        String content = getIntent().getStringExtra("postContent");
        String author = getIntent().getStringExtra("postAuthor");
        String time = getIntent().getStringExtra("postTime");

        binding.tvPostTitle.setText(title != null ? title : "게시글 제목");
        binding.tvPostBody.setText(content != null ? content : "본문 내용");
        binding.tvAuthorName.setText(author != null ? author : "작성자");
        binding.tvPostTime.setText(time != null ? time : "방금 전");

        if (author != null && !author.isEmpty()) {
            binding.tvAuthorAvatar.setText(author.substring(0, 1));
        }

        // 유저 권한 정보 및 게시글 삭제 권한 (간부 유저 또는 행정반/본인 작성자)
        String userType = getIntent().getStringExtra("userType");
        boolean canDeletePost = "간부".equals(userType) || "행정반".equals(author);

        if (canDeletePost) {
            binding.btnDeletePost.setVisibility(View.VISIBLE);
            binding.btnDeletePost.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("게시글 삭제")
                        .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            android.content.Intent resultIntent = new android.content.Intent();
                            resultIntent.putExtra("postId", postId);
                            resultIntent.putExtra("isDeleted", true);
                            setResult(RESULT_OK, resultIntent);
                            Toast.makeText(this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        } else {
            binding.btnDeletePost.setVisibility(View.GONE);
        }

        // 뒤로가기
        binding.btnBack.setOnClickListener(v -> {
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra("postId", postId);
            resultIntent.putExtra("updatedLikeCount", likeCount);
            resultIntent.putExtra("updatedCommentCount", commentList.size());
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
        });

        // 댓글 리사이클러뷰(rv_comments)에 CommentAdapter 연결
        commentList.add(new Comment("c1", "u2", "이일병", "체육대회 일정 확인했습니다!", "1시간 전"));
        commentList.add(new Comment("c2", "u3", "박상병", "준비물 챙기겠습니다.", "30분 전"));

        CommentAdapter commentAdapter = new CommentAdapter(commentList);

        commentAdapter.setOnCommentDeleteListener((position, comment) -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("댓글 삭제")
                    .setMessage("댓글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        if (position >= 0 && position < commentList.size()) {
                            commentList.remove(position);
                            commentAdapter.notifyItemRemoved(position);
                            binding.tvCommentTotal.setText(String.valueOf(commentList.size()));
                            Toast.makeText(this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        binding.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);
        binding.tvCommentTotal.setText(String.valueOf(commentList.size()));

        // [수정] 댓글 입력 후 등록 클릭 시 목록에 실시간 추가
        binding.btnSendComment.setOnClickListener(v -> {
            String commentText = binding.inputComment.getText().toString().trim();
            if (commentText.isEmpty()) {
                binding.inputComment.setError("댓글을 입력해주세요.");
                return;
            }

            Comment newComment = new Comment("c_" + System.currentTimeMillis(), "u1", "김병장", commentText, "방금 전");
            commentList.add(newComment);
            commentAdapter.notifyItemInserted(commentList.size() - 1);
            binding.tvCommentTotal.setText(String.valueOf(commentList.size()));

            binding.inputComment.setText("");
            Toast.makeText(this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLikeButtonUI() {
        binding.btnLike.setSelected(isLiked); // 셀렉터 상태값 적용 (빨간색 ↔ 회색 전환)
        binding.btnLike.setText((isLiked ? "◆ 좋아요 " : "◇ 좋아요 ") + likeCount);
        int colorRes = isLiked ? R.color.personal : R.color.text_secondary;
        binding.btnLike.setTextColor(androidx.core.content.ContextCompat.getColor(this, colorRes));
    }

}