package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemScheduleBinding;
import com.example.c20232884.model.ScheduleItem;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleItem> scheduleList;

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = ItemScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position); // 위치에 맞는 데이터 1개 추출

        holder.binding.tvScheduleTitle.setText(item.getTitle());
        holder.binding.tvStartTime.setText(item.getTimeRange()); // "19:00 ~ 20:00"
        holder.binding.tvLocation.setText(item.getLocation());

        if (item.getDate() != null && item.getDate().contains("-")) {
            try {
                // "2025-06-16"을 자바 Date 객체로 변환
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA);
                java.util.Date date = inputFormat.parse(item.getDate());

                // 날짜 출력 (예: "6.16")
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("M.d", java.util.Locale.KOREA);
                holder.binding.tvStartDate.setText(dateFormat.format(date));

                // 요일 출력 (예: "월", "화", "수")
                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("E", java.util.Locale.KOREA);
                holder.binding.tvWeekday.setText(dayFormat.format(date));

            } catch (Exception e) {
                // 예외 발생 시 원본 날짜 출력
                holder.binding.tvStartDate.setText(item.getDate());
                holder.binding.tvWeekday.setText("");
            }
        }

    }

    @Override
    public int getItemCount() {
        return (scheduleList != null) ? scheduleList.size() : 0;
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        ItemScheduleBinding binding;

        public ScheduleViewHolder(@NonNull ItemScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}