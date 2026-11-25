package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemCommentBinding;
import com.example.c20232884.model.Comment;

import java.util.List;

/**
 * 댓글 목록(item_comment.xml) 표출 및 본인 댓글 삭제를 처리하는 CommentAdapter 클래스
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnCommentDeleteListener {
        void onCommentDelete(int position, Comment comment);
    }

    private List<Comment> commentList;
    private OnCommentDeleteListener deleteListener;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void setOnCommentDeleteListener(OnCommentDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.binding.tvCommentAuthor.setText(comment.getAuthorName());
        holder.binding.tvCommentTime.setText(comment.getCreatedAt());
        holder.binding.tvCommentBody.setText(comment.getContent());

        if (comment.getAuthorName() != null && !comment.getAuthorName().isEmpty()) {
            holder.binding.tvCommentAvatar.setText(comment.getAuthorName().substring(0, 1));
        }

        // [삭제 권한] 본인(김병장)이 작성한 댓글인 경우에만 삭제 버튼 노출
        boolean isMyComment = "김병장".equals(comment.getAuthorName());
        holder.binding.btnDeleteComment.setVisibility(isMyComment ? View.VISIBLE : View.GONE);

        holder.binding.btnDeleteComment.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onCommentDelete(holder.getBindingAdapterPosition(), comment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (commentList != null) ? commentList.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        public CommentViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}