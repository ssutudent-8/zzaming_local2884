package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemCalendarDayBinding;

import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {

        void onDaySelect(String day);

        void onDayAddSchedule(String day);
    }

    private List<String> daysList;
    private OnDayClickListener listener;
    private String selectedDay = "";
    private String todayDay = "";

    public CalendarDayAdapter(List<String> daysList, String selectedDay, String todayDay, OnDayClickListener listener) {
        this.daysList = daysList;
        this.selectedDay = selectedDay;
        this.todayDay = todayDay;
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

        if (day != null && !day.isEmpty()) {
            boolean isSelected = day.equals(selectedDay);
            boolean isToday = day.equals(todayDay);

            // 1) 선택된 날짜 ➔ 날짜 셀 전체(itemView)에 bg_day_selected(연한 초록색 카드) 적용
            if (isSelected) {
                holder.itemView.setBackgroundResource(R.drawable.bg_day_selected);
            } else {
                holder.itemView.setBackground(null);
            }

            // 2) 오늘 날짜 ➔ 숫자 글자판(tvDayNumber)에 새로 만든 bg_day_today(숲색 배지) 적용
            if (isToday) {
                holder.binding.tvDayNumber.setBackgroundResource(R.drawable.bg_day_today);
                holder.binding.tvDayNumber.setTextColor(android.graphics.Color.WHITE);
            } else {
                holder.binding.tvDayNumber.setBackground(null);
                holder.binding.tvDayNumber.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
            }

            // 2단계 클릭 이벤트 로직
            holder.itemView.setOnClickListener(v -> {
                if (listener == null) return;

                if (day.equals(selectedDay)) {
                    listener.onDayAddSchedule(day);
                } else {
                    selectedDay = day;
                    notifyDataSetChanged();
                    listener.onDaySelect(day);
                }
            });
        } else {
            holder.itemView.setBackground(null);
            holder.binding.tvDayNumber.setBackground(null);
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