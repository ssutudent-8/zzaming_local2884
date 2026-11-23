package com.example.c20232884;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
    private String userType = "병사";

    // 새 글 작성 수신 런처
    private final ActivityResultLauncher<Intent> composeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String title = data.getStringExtra("newPostTitle");
                    String body = data.getStringExtra("newPostBody");
                    String author = data.getStringExtra("newPostAuthor");

                    Post newPost = new Post("p_" + System.currentTimeMillis(), title, body, "", author, "방금 전", new ArrayList<>());
                    postList.add(0, newPost);
                    filterPostsByCategory(currentCategory);
                }
            });

    // 게시글 상세 & 삭제 수신 런처
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String postId = data.getStringExtra("postId");
                    boolean isDeleted = data.getBooleanExtra("isDeleted", false);
                    int newLikeCount = data.getIntExtra("updatedLikeCount", -1);

                    if (postId != null && !postId.isEmpty()) {
                        for (int i = 0; i < postList.size(); i++) {
                            Post post = postList.get(i);
                            if (postId.equals(post.getPostId())) {
                                if (isDeleted) {
                                    postList.remove(i);
                                    Toast.makeText(this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (newLikeCount != -1) post.setLikes(newLikeCount);
                                }
                                filterPostsByCategory(currentCategory);
                                break;
                            }
                        }
                    }
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

        String passedUserType = getIntent().getStringExtra("userType");
        if (passedUserType != null) {
            userType = passedUserType;
        }

        binding.btnBack.setOnClickListener(v -> finish());

        if ("간부".equals(userType)) {
            binding.btnInbox.setVisibility(View.VISIBLE);
            binding.btnInbox.setOnClickListener(v -> filterPostsByCategory("마음의 편지"));
        } else {
            binding.btnInbox.setVisibility(View.GONE);
        }

        initDummyPosts();

        filteredPostList = new ArrayList<>(postList);
        postAdapter = new PostAdapter(filteredPostList);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPosts.setAdapter(postAdapter);

        setupFilterTabs();

        binding.btnCompose.setOnClickListener(v -> {
            Intent intent = new Intent(PostActivity.this, PostComposeActivity.class);
            intent.putExtra("userType", userType);
            composeLauncher.launch(intent);
        });

        postAdapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(PostActivity.this, PostDetailActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("postTitle", post.getTitle());
            intent.putExtra("postContent", post.getContent());
            intent.putExtra("postAuthor", post.getAuthorName());
            intent.putExtra("postTime", post.getCreatedAt());
            intent.putExtra("userType", userType);
            detailLauncher.launch(intent);
        });
    }

    private void initDummyPosts() {
        postList = new ArrayList<>();

        List<Comment> comments1 = new ArrayList<>();
        comments1.add(new Comment("c1", "user2", "이일병", "체육대회 일정 확인했습니다!", "1시간 전"));
        comments1.add(new Comment("c2", "user3", "박상병", "준비물 챙기겠습니다.", "30분 전"));

        postList.add(new Post(
                "p1",
                "6월 대대 체육대회 및 일정 안내",
                "이번 주 금요일 대대 전투체육 일정 안내드립니다. 모두 전투복 및 운동화 착용 바랍니다.",
                "user1",
                "행정반",
                "1시간 전",
                comments1
        ));

        postList.add(new Post(
                "p2",
                "마음의 편지함 점검 안내",
                "익명성이 보장되는 부대 개선 마음의 편지함이 열려 있으니 많은 의견 바랍니다.",
                "user1",
                "주임원사",
                "3시간 전",
                new ArrayList<>()
        ));
    }

    private void setupFilterTabs() {
        binding.filterAll.setOnClickListener(v -> filterPostsByCategory("ALL"));
        binding.filterNotice.setOnClickListener(v -> filterPostsByCategory("공지"));
        binding.filterFree.setOnClickListener(v -> filterPostsByCategory("자유"));
        binding.filterQuestion.setOnClickListener(v -> filterPostsByCategory("질문"));

        updateFilterTabStyles();
    }

    private void filterPostsByCategory(String category) {
        this.currentCategory = category;
        updateFilterTabStyles();

        filteredPostList.clear();
        for (Post post : postList) {
            if ("ALL".equals(category)) {
                if (!"마음의 편지".equals(post.getTitle()) && !"익명".equals(post.getAuthorName())) {
                    filteredPostList.add(post);
                }
            } else if (category.equals(post.getTitle()) || category.equals(post.getAuthorName()) || "마음의 편지".equals(category)) {
                if ("마음의 편지".equals(category) && "익명".equals(post.getAuthorName())) {
                    filteredPostList.add(post);
                } else if (!"마음의 편지".equals(category)) {
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
