package com.example.c20232884;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.c20232884.databinding.ActivityBoardBinding;
import com.example.c20232884.model.Comment;
import com.example.c20232884.model.Post;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {

    private ActivityBoardBinding binding;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.boardRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());

        //게시글 더미 데이터
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

        BoardAdapter adapter = new BoardAdapter(postList);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPosts.setAdapter(adapter);
    }
}