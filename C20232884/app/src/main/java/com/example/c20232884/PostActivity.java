package com.example.c20232884;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.c20232884.databinding.ActivityPostBinding;
import com.example.c20232884.model.Comment;
import com.example.c20232884.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * 부대 게시글 목록 표출, 카테고리 필터링, 유저 권한별 익명 마음의 편지글 및 새 글 작성을 담당하는 메인 게시글 액티비티 클래스
 */
public class PostActivity extends AppCompatActivity {

    private ActivityPostBinding binding;
    private List<Post> postList;
    private List<Post> filteredPostList;
    private PostAdapter postAdapter;
    private String currentCategory = "ALL";
    private String memberType = "병사";

    // 새 글 작성 수신 런처
    private final ActivityResultLauncher<Intent> composeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadPostsFromServer();
                }
            });

    // 게시글 상세 & 삭제 수신 런처
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadPostsFromServer();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.postRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String passedMemberType = getIntent().getStringExtra("memberType");
        if (passedMemberType != null) {
            memberType = passedMemberType;
        }

        String startCategory = getIntent().getStringExtra("startCategory");

        binding.btnBack.setOnClickListener(v -> finish());

        if ("간부".equals(memberType)) {
            binding.btnInbox.setVisibility(View.VISIBLE);
            binding.btnInbox.setOnClickListener(v -> {
                String currentBtnText = binding.btnInbox.getText().toString();
                if ("마음의 편지함".equals(currentBtnText)) {
                    // 마음의 편지함 진입 2차 확인 다이얼로그
                    new AlertDialog.Builder(this)
                            .setTitle("마음의 편지 수신함 진입")
                            .setMessage("무기명 보안 수신함으로 이동하시겠습니까?\n(외부 유출 주의해 주시기 바랍니다)")
                            .setPositiveButton("진입", (dialog, which) -> {
                                binding.layoutFilterTabs.setVisibility(View.GONE);
                                binding.btnCompose.setVisibility(View.GONE);
                                binding.tvPostHeaderTitle.setText("📬 마음의 편지 수신함");
                                binding.tvPostSubTitle.setText("대원 무기명 건의 및 고충 수신 내역");
                                binding.postEmpty.setText("새로 접수된 마음의 편지가 없습니다.");
                                binding.btnInbox.setText("부대 게시판");
                                filterPostsByCategory("마음의 편지");
                            })
                            .setNegativeButton("취소", null)
                            .show();
                } else {
                    // 부대 게시판 복귀 2차 확인 다이얼로그
                    new AlertDialog.Builder(this)
                            .setTitle("부대 게시판 이동")
                            .setMessage("일반 부대 게시판으로 돌아가시겠습니까?")
                            .setPositiveButton("이동", (dialog, which) -> {
                                resetGeneralBoardHeader();
                                filterPostsByCategory("ALL");
                            })
                            .setNegativeButton("취소", null)
                            .show();
                }
            });

            if ("마음의 편지".equals(startCategory)) {
                binding.layoutFilterTabs.setVisibility(View.GONE);
                binding.btnCompose.setVisibility(View.GONE);
                binding.tvPostHeaderTitle.setText("📬 마음의 편지 수신함");
                binding.tvPostSubTitle.setText("대원 무기명 건의 및 고충 수신 내역");
                binding.postEmpty.setText("새로 접수된 마음의 편지가 없습니다.");
                binding.btnInbox.setText("부대 게시판");
                currentCategory = "마음의 편지";
            }
        } else {
            binding.btnInbox.setVisibility(View.GONE);
        }

        postList = new ArrayList<>();
        filteredPostList = new ArrayList<>();
        postAdapter = new PostAdapter(filteredPostList);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPosts.setAdapter(postAdapter);

        setupFilterTabs();
        loadPostsFromServer();

        binding.btnCompose.setOnClickListener(v -> {
            Intent intent = new Intent(PostActivity.this, PostComposeActivity.class);
            intent.putExtra("memberType", memberType);
            composeLauncher.launch(intent);
        });

        postAdapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(PostActivity.this, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("postTitle", post.getTitle());
            intent.putExtra("postContent", post.getContent());
            intent.putExtra("postAuthor", post.getAuthorName());
            intent.putExtra("postAuthorUid", post.getAuthorUid());
            intent.putExtra("postTime", post.getCreatedAt());
            intent.putExtra("memberType", memberType);

            if (post.getComments() != null) {
                intent.putExtra("postComments", (java.io.Serializable) post.getComments());
            }
            detailLauncher.launch(intent);
        });
    }

    private void loadPostsFromServer() {
        MockServerDataSource.getInstance(this).fetchServerPosts(new MockServerDataSource.OnPostLoadListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                postList.clear();
                if (posts != null) {
                    postList.addAll(posts);
                }
                filterPostsByCategory(currentCategory);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PostActivity.this, "게시글 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterTabs() {
        binding.filterAll.setOnClickListener(v -> {
            resetGeneralBoardHeader();
            filterPostsByCategory("ALL");
        });
        binding.filterNotice.setOnClickListener(v -> {
            resetGeneralBoardHeader();
            filterPostsByCategory("공지");
        });
        binding.filterFree.setOnClickListener(v -> {
            resetGeneralBoardHeader();
            filterPostsByCategory("자유");
        });
        binding.filterQuestion.setOnClickListener(v -> {
            resetGeneralBoardHeader();
            filterPostsByCategory("질문");
        });

        updateFilterTabStyles();
    }

    private void resetGeneralBoardHeader() {
        binding.layoutFilterTabs.setVisibility(View.VISIBLE);
        binding.btnCompose.setVisibility(View.VISIBLE);
        binding.tvPostHeaderTitle.setText("부대 게시판");
        binding.tvPostSubTitle.setText("대원들과 소식을 나눠요");
        binding.postEmpty.setText("아직 등록된 글이 없습니다.");
        binding.btnInbox.setText("마음의 편지함");
    }

    private void filterPostsByCategory(String category) {
        this.currentCategory = category;
        updateFilterTabStyles();

        filteredPostList.clear();
        for (Post post : postList) {
            String postCategory = (post.getCategory() != null) ? post.getCategory() : "자유";

            if ("ALL".equals(category)) {
                if (!"마음의 편지".equals(postCategory)) {
                    filteredPostList.add(post);
                }
            } else if ("마음의 편지".equals(category)) {
                if ("마음의 편지".equals(postCategory)) {
                    filteredPostList.add(post);
                }
            } else {
                if (category.equals(postCategory)) {
                    filteredPostList.add(post);
                }
            }
        }

        if (filteredPostList.isEmpty()) {
            binding.postEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.postEmpty.setVisibility(View.GONE);
        }

        if (postAdapter != null) {
            postAdapter.notifyDataSetChanged();
        }
    }

    private void updateFilterTabStyles() {
        binding.filterAll.setSelected("ALL".equals(currentCategory));
        binding.filterNotice.setSelected("공지".equals(currentCategory));
        binding.filterFree.setSelected("자유".equals(currentCategory));
        binding.filterQuestion.setSelected("질문".equals(currentCategory));
    }
}
