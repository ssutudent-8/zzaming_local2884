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

        boolean isMindLetter = "마음의 편지".equals(post.getCategory());

        if (isMindLetter) {
            // [수정] 마음의 편지 카드: 메타 정보(작성자/시간), 좋아요, 댓글 숨김 및 보안 대체 텍스트 표출
            holder.binding.tvPostMeta.setVisibility(View.GONE);
            holder.binding.btnPostLike.setVisibility(View.GONE);
            holder.binding.btnPostComment.setVisibility(View.GONE);

            holder.binding.tvPostCategory.setText("🔒 마음의 편지");
            holder.binding.tvPostCategory.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.personal));
            holder.binding.tvPostBody.setText("🔒 보안 유지 및 무기명 보호를 위해 내용이 미리보기로 공개되지 않습니다. 클릭하여 수신 본문을 확인하세요.");
        } else {
            // 일반 게시글 카드: 정상 표출
            holder.binding.tvPostMeta.setVisibility(View.VISIBLE);
            holder.binding.btnPostLike.setVisibility(View.VISIBLE);
            holder.binding.btnPostComment.setVisibility(View.VISIBLE);

            String categoryStr = (post.getCategory() != null) ? post.getCategory() : "자유";
            holder.binding.tvPostCategory.setText(categoryStr);
            holder.binding.tvPostCategory.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_700));

            holder.binding.tvPostBody.setText(post.getContent());
            holder.binding.tvPostMeta.setText(post.getAuthorName() + " · " + post.getCreatedAt());

            holder.binding.btnPostComment.setText("댓글 " + post.getCommentCount());
            holder.binding.btnPostLike.setText("좋아요 " + post.getLikes());
        }

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
