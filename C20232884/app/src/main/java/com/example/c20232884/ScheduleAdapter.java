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
        void onItemSelectDate(ScheduleItem item); // 카드 클릭: 해당 일자 및 월로 달력 이동
        void onItemDetailClick(ScheduleItem item); // 점 3개 클릭: 상세정보 팝업 활성화
    }

    private static final int TYPE_SCHEDULE = 0;
    private static final int TYPE_VACATION = 1;

    private List<ScheduleItem> scheduleList;
    private OnItemClickListener listener;
    private String todayStr = "";
    private String currentYM = "";

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCalendarContext(String todayStr, String currentYM) {
        this.todayStr = (todayStr != null) ? todayStr : "";
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

        // 카드 터치 시 날짜 및 월 선택 동기화 리스너
        View.OnClickListener selectDateListener = v -> {
            if (listener != null) {
                listener.onItemSelectDate(item);
            }
        };

        holder.itemView.setOnClickListener(selectDateListener);

        // [수정] 내부 카드 레이아웃(scheduleItem/vacationItem)에도 날짜 선택 터치 이벤트 이중 바인딩
        if (holder instanceof ScheduleViewHolder) {
            ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
            bindScheduleItem(scheduleHolder, item, isSectionChanged);

            scheduleHolder.binding.scheduleItem.setOnClickListener(selectDateListener); // 카드 본체 터치 ➔ 날짜 선택
            scheduleHolder.binding.btnItemMore.setOnClickListener(v -> {                // 3선 버튼 터치 ➔ 팝업
                if (listener != null) {
                    listener.onItemDetailClick(item);
                }
            });
        } else if (holder instanceof VacationViewHolder) {
            VacationViewHolder vacationHolder = (VacationViewHolder) holder;
            bindVacationItem(vacationHolder, item, isSectionChanged);

            vacationHolder.binding.vacationItem.setOnClickListener(selectDateListener); // 카드 본체 터치 ➔ 날짜 선택
            vacationHolder.binding.btnItemMore.setOnClickListener(v -> {                // 3선 버튼 터치 ➔ 팝업
                if (listener != null) {
                    listener.onItemDetailClick(item);
                }
            });
        }
    }

    private void bindScheduleItem(ScheduleViewHolder holder, ScheduleItem item, boolean isSectionChanged) {
        holder.binding.dividerLine.setVisibility(isSectionChanged ? View.VISIBLE : View.GONE);
        holder.binding.tvScheduleTitle.setText(item.getTitle());

        if (item.getLocation() != null && !item.getLocation().isEmpty()) {
            holder.binding.tvLocation.setVisibility(View.VISIBLE);
            holder.binding.tvLocation.setText(item.getLocation());
        } else {
            holder.binding.tvLocation.setVisibility(View.GONE);
        }

        String type = (item.getType() != null) ? item.getType() : "";
        int colorRes = "부대".equals(type) ? R.color.forest_700 : ("휴가".equals(type) ? R.color.vacation_text : R.color.personal);
        int barColorRes = "부대".equals(type) ? R.color.forest_500 : ("휴가".equals(type) ? R.color.vacation : R.color.personal);

        int textColor = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        int barColor = ContextCompat.getColor(holder.itemView.getContext(), barColorRes);

        holder.binding.categoryBar.setBackgroundColor(barColor);
        holder.binding.tvScheduleTitle.setTextColor(textColor);
        holder.binding.tvStartDate.setTextColor(textColor);
        holder.binding.tvWeekdayTime.setTextColor(textColor);
        holder.binding.tvLocation.setTextColor(textColor);



        if (item.getDate() != null && item.getDate().contains("-")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                Date date = inputFormat.parse(item.getDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd", Locale.KOREA);
                holder.binding.tvStartDate.setText(dateFormat.format(date));

                SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.KOREA);
                String weekday = dayFormat.format(date);
                String startTime = (item.getStartTime() != null) ? item.getStartTime() : "";
                holder.binding.tvWeekdayTime.setText(weekday + " " + startTime);
            } catch (Exception e) {
                holder.binding.tvStartDate.setText(item.getDate());
                holder.binding.tvWeekdayTime.setText(item.getTimeRange());
            }
        }
    }

    private void bindVacationItem(VacationViewHolder holder, ScheduleItem item, boolean isSectionChanged) {
        holder.binding.dividerLine.setVisibility(isSectionChanged ? View.VISIBLE : View.GONE);
        holder.binding.tvVacationTitle.setText(item.getTitle());
        holder.binding.tvVacationDurationBadge.setText("4일"); // 기간 뱃지

        if (item.getDate() != null && item.getDate().contains("-")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                Date date = inputFormat.parse(item.getDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd", Locale.KOREA);
                String startStr = dateFormat.format(date);
                holder.binding.tvVacationStart.setText(startStr);
                holder.binding.tvVacationEndSub.setText("~ " + startStr);

                SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.KOREA);
                String weekday = dayFormat.format(date);
                holder.binding.tvVacationFlowSub.setText(startStr + "(" + weekday + ") 출발 · 복귀 예정");
            } catch (Exception e) {
                holder.binding.tvVacationStart.setText(item.getDate());
                holder.binding.tvVacationEndSub.setText("~ " + item.getDate());
                holder.binding.tvVacationFlowSub.setText(item.getDate() + " 출발");
            }
        }
    }

    private String getSection(ScheduleItem item) {
        if (item == null || item.getDate() == null) return "UNKNOWN";

        String date = item.getDate();

        if (todayStr != null && !todayStr.isEmpty() && date.equals(todayStr)) {
            return "TODAY";
        } else if (todayStr != null && !todayStr.isEmpty() && date.compareTo(todayStr) < 0) {
            return "PAST"; // 오늘 이전 모든 일정은 PAST(지난 일정, 회색 처리)로 분류
        } else if (currentYM != null && !currentYM.isEmpty() && date.startsWith(currentYM)) {
            return "THIS_MONTH";
        } else {
            return "FUTURE";
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
