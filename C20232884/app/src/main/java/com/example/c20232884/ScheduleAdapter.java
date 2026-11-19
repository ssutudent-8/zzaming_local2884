package com.example.c20232884;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c20232884.databinding.ItemScheduleBinding;
import com.example.c20232884.databinding.ItemVacationScheduleBinding;
import com.example.c20232884.model.ScheduleItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 일반 일정 및 휴가 일정을 표출하고 지난 일정을 회색 처리하며 구역 경계선 및 클릭 이벤트를 제어하는 어댑터 클래스
 */
public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ScheduleItem item);
    }

    private static final int TYPE_SCHEDULE = 0;
    private static final int TYPE_VACATION = 1;

    private List<ScheduleItem> scheduleList;
    private OnItemClickListener listener;
    private String targetDateStr = "";
    private String currentYM = "";

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCalendarContext(String targetDateStr, String currentYM) {
        this.targetDateStr = (targetDateStr != null) ? targetDateStr : "";
        this.currentYM = (currentYM != null) ? currentYM : "";
    }

    @Override
    public int getItemViewType(int position) {
        ScheduleItem item = scheduleList.get(position);
        if (item != null && "휴가".equals(item.getType())) {
            return TYPE_VACATION;
        }
        return TYPE_SCHEDULE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VACATION) {
            ItemVacationScheduleBinding binding = ItemVacationScheduleBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new VacationViewHolder(binding);
        } else {
            ItemScheduleBinding binding = ItemScheduleBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ScheduleViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position);
        if (item == null) return;

        boolean isSectionChanged = false;
        if (position > 0) {
            ScheduleItem previousItem = scheduleList.get(position - 1);
            isSectionChanged = !getSection(item).equals(getSection(previousItem));
        }

        boolean isPast = "PAST".equals(getSection(item));
        holder.itemView.setAlpha(isPast ? 0.55f : 1.0f);

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        };

        if (holder instanceof ScheduleViewHolder) {
            ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
            bindScheduleItem(scheduleHolder, item, isSectionChanged);
            scheduleHolder.binding.scheduleItem.setOnClickListener(clickListener);
        } else if (holder instanceof VacationViewHolder) {
            VacationViewHolder vacationHolder = (VacationViewHolder) holder;
            bindVacationItem(vacationHolder, item, isSectionChanged);
            vacationHolder.binding.vacationItem.setOnClickListener(clickListener);
        }

        holder.itemView.setOnClickListener(clickListener);
    }

    private void bindScheduleItem(ScheduleViewHolder holder, ScheduleItem item, boolean isSectionChanged) {
        holder.binding.dividerLine.setVisibility(isSectionChanged ? View.VISIBLE : View.GONE);
        holder.binding.tvScheduleTitle.setText(item.getTitle());
        holder.binding.tvStartTime.setText(item.getTimeRange());
        holder.binding.tvLocation.setText(item.getLocation());

        String type = (item.getType() != null) ? item.getType() : "";
        int colorRes;
        int barColorRes;

        if ("부대".equals(type)) {
            colorRes = R.color.forest_700;
            barColorRes = R.color.forest_500;
        } else if ("휴가".equals(type)) {
            colorRes = R.color.vacation_text;
            barColorRes = R.color.vacation;
        } else {
            colorRes = R.color.personal;
            barColorRes = R.color.personal;
        }

        int textColor = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        int barColor = ContextCompat.getColor(holder.itemView.getContext(), barColorRes);

        holder.binding.categoryBar.setBackgroundColor(barColor);
        holder.binding.tvStartDate.setTextColor(textColor);
        holder.binding.tvStartTime.setTextColor(textColor);
        holder.binding.tvLocation.setTextColor(textColor);

        if (item.getDate() != null && item.getDate().contains("-")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                Date date = inputFormat.parse(item.getDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("M.d", Locale.KOREA);
                holder.binding.tvStartDate.setText(dateFormat.format(date));

                SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.KOREA);
                holder.binding.tvWeekday.setText(dayFormat.format(date));
            } catch (Exception e) {
                holder.binding.tvStartDate.setText(item.getDate());
                holder.binding.tvWeekday.setText("");
            }
        }
    }

    private void bindVacationItem(VacationViewHolder holder, ScheduleItem item, boolean isSectionChanged) {
        holder.binding.dividerLine.setVisibility(isSectionChanged ? View.VISIBLE : View.GONE);
        holder.binding.tvVacationTitle.setText(item.getTitle());

        if (item.getDate() != null && item.getDate().contains("-")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                Date date = inputFormat.parse(item.getDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("M.d", Locale.KOREA);
                holder.binding.tvVacationStart.setText(dateFormat.format(date));
                holder.binding.tvVacationEnd.setText(dateFormat.format(date));

                SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.KOREA);
                holder.binding.tvVacationStartWeekday.setText(dayFormat.format(date));
                holder.binding.tvVacationEndWeekday.setText(dayFormat.format(date));
            } catch (Exception e) {
                holder.binding.tvVacationStart.setText(item.getDate());
                holder.binding.tvVacationEnd.setText(item.getDate());
            }
        }
        holder.binding.tvVacationDuration.setText("1일");
    }

    private String getSection(ScheduleItem item) {
        if (item == null || item.getDate() == null) return "UNKNOWN";

        String date = item.getDate();

        if (targetDateStr != null && !targetDateStr.isEmpty() && date.equals(targetDateStr)) {
            return "SELECTED";
        } else if (currentYM != null && !currentYM.isEmpty() && date.startsWith(currentYM)) {
            return "THIS_MONTH";
        } else if (currentYM != null && !currentYM.isEmpty() && date.compareTo(currentYM + "-31") > 0) {
            return "FUTURE";
        } else {
            return "PAST";
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

    public static class VacationViewHolder extends RecyclerView.ViewHolder {
        ItemVacationScheduleBinding binding;

        public VacationViewHolder(@NonNull ItemVacationScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
