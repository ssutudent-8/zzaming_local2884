package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemPostBinding;
import com.example.c20232884.model.Post;

import java.util.List;

/**
 * 게시글 목록 데이터를 item_post.xml 뷰에 연결하는 PostAdapter 클래스
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    private List<Post> postList;
    private OnItemClickListener listener;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.binding.tvPostTitle.setText(post.getTitle());
        holder.binding.tvPostBody.setText(post.getContent());
        holder.binding.tvPostMeta.setText(post.getAuthorName() + " · " + post.getCreatedAt());

        int commentCount = (post.getComments() != null) ? post.getComments().size() : 0;
        holder.binding.btnPostComment.setText("댓글 " + commentCount);
        holder.binding.btnPostLike.setText("좋아요 " + post.getLikes());

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onItemClick(post);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.binding.postOpenArea.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return (postList != null) ? postList.size() : 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;

        public PostViewHolder(@NonNull ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
