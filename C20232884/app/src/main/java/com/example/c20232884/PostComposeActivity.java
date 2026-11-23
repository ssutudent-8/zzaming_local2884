package com.example.c20232884;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.c20232884.databinding.DialogPostComposeBinding;
import com.example.c20232884.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * 새 글 작성 및 익명 마음의 편지를 전용 새 창 화면으로 처리하는 액티비티 클래스
 */
public class PostComposeActivity extends AppCompatActivity {

    private DialogPostComposeBinding binding;
    private String userType = "병사";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogPostComposeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String passedUserType = getIntent().getStringExtra("userType");
        if (passedUserType != null) {
            userType = passedUserType;
        }

        // 스피너 카테고리 구성
        List<String> categories = new ArrayList<>();
        if ("간부".equals(userType)) {
            categories.add("공지");
        }
        categories.add("자유");
        categories.add("질문");
        categories.add("마음의 편지");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, categories
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
                    tv.setTextSize(14);
                    tv.setPadding(16, 0, 16, 0);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
                    tv.setTextSize(14);
                    tv.setPadding(24, 24, 24, 24);
                }
                return v;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(spinnerAdapter);

        // 닫기 버튼
        binding.btnCloseCompose.setOnClickListener(v -> finish());

        // 게시하기 버튼
        binding.btnPublishPost.setOnClickListener(v -> {
            String title = binding.inputPostTitle.getText().toString().trim();
            String body = binding.inputPostBody.getText().toString().trim();
            String selectedCategory = (String) binding.spinnerCategory.getSelectedItem();

            if (title.isEmpty()) {
                binding.inputPostTitle.setError("제목을 입력해주세요.");
                return;
            }
            if (body.isEmpty()) {
                binding.inputPostBody.setError("내용을 입력해주세요.");
                return;
            }

            boolean isAnonymous = "마음의 편지".equals(selectedCategory);
            String authorName = isAnonymous ? "익명" : ("간부".equals(userType) ? "행정반" : "김병장");
            String authorId = isAnonymous ? "" : "user1";

            Post newPost = new Post(
                    "p_" + System.currentTimeMillis(),
                    title,
                    body,
                    authorId,
                    authorName,
                    "방금 전",
                    new ArrayList<>()
            );

            Intent resultIntent = new Intent();
            resultIntent.putExtra("newPostTitle", newPost.getTitle());
            resultIntent.putExtra("newPostBody", newPost.getContent());
            resultIntent.putExtra("newPostAuthor", newPost.getAuthorName());
            resultIntent.putExtra("newPostCategory", selectedCategory);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, isAnonymous ? "익명 마음의 편지가 작성되었습니다." : "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}