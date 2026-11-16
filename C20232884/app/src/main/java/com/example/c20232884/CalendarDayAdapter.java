package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemCalendarDayBinding;

import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    private List<String> daysList;

    public CalendarDayAdapter(List<String> daysList) {
        this.daysList = daysList;
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