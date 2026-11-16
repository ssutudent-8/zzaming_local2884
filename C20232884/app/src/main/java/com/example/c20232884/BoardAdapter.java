package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemPostBinding;
import com.example.c20232884.model.Post;

import java.util.List;

/**
 * 게시판 목록 데이터를 item_post.xml 뷰에 연결하는 BoardAdapter 클래스
 */
public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    private List<Post> postList;

    public BoardAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new BoardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Post post = postList.get(position);

        // 게시글 제목, 본문 내용, 작성자 및 작성 시간 세팅
        holder.binding.tvPostTitle.setText(post.getTitle());
        holder.binding.tvPostBody.setText(post.getContent());
        holder.binding.tvPostMeta.setText(post.getAuthorName() + " · " + post.getCreatedAt());

        // 댓글 개수 세팅
        int commentCount = (post.getComments() != null) ? post.getComments().size() : 0;
        holder.binding.btnPostComment.setText("댓글 " + commentCount);
    }

    @Override
    public int getItemCount() {
        return (postList != null) ? postList.size() : 0;
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;

        public BoardViewHolder(@NonNull ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}