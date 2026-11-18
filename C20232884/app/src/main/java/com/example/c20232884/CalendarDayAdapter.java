package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemCalendarDayBinding;

import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(String day);
    }

    private List<String> daysList;
    private OnDayClickListener listener;

    public CalendarDayAdapter(List<String> daysList, OnDayClickListener listener) {
        this.daysList = daysList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        String day = daysList.get(position);
        holder.binding.tvDayNumber.setText(day);

        // 빈 칸이 아닌 실제 날짜 숫자인 경우에만 클릭 이벤트 바인딩
        if (day != null && !day.isEmpty()) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return daysList != null ? daysList.size() : 0;
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        ItemCalendarDayBinding binding;

        public DayViewHolder(@NonNull ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}