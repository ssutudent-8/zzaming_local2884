package com.example.c20232884;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemCalendarDayBinding;
import com.example.c20232884.model.ScheduleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 달력 날짜 그리드를 표출하고 휴가 면적 채우기 및 최대 6개 동적 도트를 표시하는 어댑터 클래스
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDaySelect(String day);
        void onDayAddSchedule(String day);
    }

    private List<String> daysList;
    private OnDayClickListener listener;
    private String selectedFullDate = "";
    private String todayDay = "";
    private List<ScheduleItem> allScheduleList;
    private String currentYM = "";

    public CalendarDayAdapter(List<String> daysList, String selectedFullDate, String todayDay,
                              List<ScheduleItem> allScheduleList, String currentYM,
                              OnDayClickListener listener) {
        this.daysList = daysList;
        this.selectedFullDate = (selectedFullDate != null) ? selectedFullDate : "";
        this.todayDay = todayDay;
        this.allScheduleList = allScheduleList;
        this.currentYM = (currentYM != null) ? currentYM : "";
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
            String tempFullDate = "";
            try {
                int dayInt = Integer.parseInt(day);
                tempFullDate = String.format(Locale.KOREA, "%s-%02d", currentYM, dayInt);
            } catch (Exception ignored) {}

            // [수정] 람다식 내부 사용을 위해 final 변수로 고정 선언 (빨간 줄 해결)
            final String fullDateStr = tempFullDate;

            // 연-월-일(YYYY-MM-DD) 전체 유일 선택 상태 비교
            boolean isSelected = !fullDateStr.isEmpty() && fullDateStr.equals(selectedFullDate);
            boolean isToday = day.equals(todayDay);

            List<Integer> dotColors = new ArrayList<>();
            boolean hasVacation = false;

            if (allScheduleList != null && !fullDateStr.isEmpty()) {
                int unitColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_500);
                int personalColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.personal);

                List<Integer> unitDotList = new ArrayList<>();
                List<Integer> personalDotList = new ArrayList<>();

                for (ScheduleItem item : allScheduleList) {
                    if (fullDateStr.equals(item.getDate())) {
                        String type = item.getType();
                        if ("부대".equals(type)) unitDotList.add(unitColor);
                        else if ("개인".equals(type)) personalDotList.add(personalColor);
                        else if ("휴가".equals(type)) hasVacation = true;
                    }
                }

                // 부대 일정(초록점) 우선 배치 후 개인 일정(빨간점) 배치
                dotColors.addAll(unitDotList);
                dotColors.addAll(personalDotList);

                // 최대 6개 제한 (초과 시 뒤에 있는 개인 일정부터 우선 탈락)
                if (dotColors.size() > 6) {
                    dotColors = dotColors.subList(0, 6);
                }
            }

            // 1) 배경 및 텍스트 색상 설정
            holder.binding.tvDayNumber.setBackground(null);

            if (hasVacation) {
                if (isToday && isSelected) {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_900));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_today_selected);
                } else if (isToday) {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_700));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_today);
                } else if (isSelected) {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_today_vacation);
                } else {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_vacation);
                }
            } else {
                // 일반 날짜 배경 및 테두리 설정 (전체 칸 감싸기)
                if (isToday && isSelected) {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_900));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_today_selected);
                } else if (isToday) {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_700));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_today);
                } else if (isSelected) {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.forest_900));
                    holder.itemView.setBackgroundResource(R.drawable.bg_day_selected);
                } else {
                    holder.binding.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
                    holder.itemView.setBackground(null);
                }
            }

            // 2) 도트 바인딩: 배경 위로 최대 6개 동적 도트 선명하게 바인딩
            View[] dots = new View[]{
                    holder.binding.dot1, holder.binding.dot2, holder.binding.dot3,
                    holder.binding.dot4, holder.binding.dot5, holder.binding.dot6
            };

            if (!dotColors.isEmpty()) {
                holder.binding.eventDotsContainer.setVisibility(View.VISIBLE);
                for (int i = 0; i < dots.length; i++) {
                    if (i < dotColors.size()) {
                        dots[i].setVisibility(View.VISIBLE);
                        dots[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColors.get(i)));
                    } else {
                        dots[i].setVisibility(View.GONE);
                    }
                }
            } else {
                holder.binding.eventDotsContainer.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener == null) return;
                // 선택 상태 비교 시 fullDateStr로 유일 비교
                if (!fullDateStr.isEmpty() && fullDateStr.equals(selectedFullDate)) {
                    listener.onDayAddSchedule(day);
                } else {
                    selectedFullDate = fullDateStr;
                    notifyDataSetChanged();
                    listener.onDaySelect(day);
                }
            });
        } else {
            holder.itemView.setBackground(null);
            holder.binding.tvDayNumber.setBackground(null);
            holder.binding.eventDotsContainer.setVisibility(View.GONE);
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