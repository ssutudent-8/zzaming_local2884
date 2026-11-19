package com.example.c20232884;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.c20232884.databinding.ActivityCalendarBinding;
import com.example.c20232884.databinding.DialogScheduleAddBinding;
import com.example.c20232884.databinding.DialogScheduleDetailBinding;
import com.example.c20232884.model.ScheduleItem;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private ActivityCalendarBinding binding;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private List<ScheduleItem> allScheduleList = new ArrayList<>();
    private Calendar currentCalendar;
    private ScheduleAdapter scheduleAdapter;
    private List<String> serviceMonthList = new ArrayList<>();
    private int currentMonthIndex = 0;
    private String selectedDayNumber = "";
    private String currentFilter = "THIS_MONTH";
    private android.content.BroadcastReceiver dateChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.calendarRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnYear.setOnClickListener(v -> showEnlistmentDatePicker());
        binding.btnToday.setOnClickListener(v -> moveToToday());

        // 일정 시트 확장
        bottomSheetBehavior = BottomSheetBehavior.from(binding.scheduleSheet.getRoot());
        bottomSheetBehavior.setFitToContents(true);

        int peekHeightPx = (int) (400 * getResources().getDisplayMetrics().density);
        bottomSheetBehavior.setPeekHeight(peekHeightPx);

        int calculatedPaddingPx = (int) ((432) * getResources().getDisplayMetrics().density);
        binding.scheduleSheet.rvSchedules.setClipToPadding(false);
        binding.scheduleSheet.rvSchedules.setPadding(0, 0, 0, calculatedPaddingPx);

        binding.scheduleSheet.btnScheduleHandle.setOnClickListener(v -> toggleScheduleSheet());

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.scheduleSheet.btnAddSchedule.setVisibility(View.VISIBLE);
                    binding.scheduleSheet.rvSchedules.setPadding(0, 0, 0, 0);

                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.dimOverlay.setVisibility(View.GONE);
                    binding.scheduleSheet.btnAddSchedule.setVisibility(View.GONE);

                    int calculatedPaddingPx = (int) ((432) * getResources().getDisplayMetrics().density);
                    binding.scheduleSheet.rvSchedules.setClipToPadding(false);
                    binding.scheduleSheet.rvSchedules.setPadding(0, 0, 0, calculatedPaddingPx);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0) {
                    binding.dimOverlay.setVisibility(View.VISIBLE);
                    binding.dimOverlay.setAlpha(slideOffset);
                }
            }
        });

        binding.dimOverlay.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.btnBack.setOnClickListener(v -> finish());

        // 버튼으로 일정 추가
        binding.scheduleSheet.btnAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        // 1. 더미 데이터 초기화
        initDummySchedules();

        // 2. 달력 시계 초기화
        currentCalendar = Calendar.getInstance();

        // 3. 탭 클릭 이벤트 연결
        setupTabListeners();
        initTodayViews();

        // 4. 어댑터 연결 및 클릭 리스너 설정
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        scheduleAdapter.setOnItemClickListener(item -> showScheduleDetailDialog(item));
        binding.scheduleSheet.rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.scheduleSheet.rvSchedules.setAdapter(scheduleAdapter);

        // ◀ 이전 달 버튼 클릭
        binding.btnPreviousMonth.setOnClickListener(v -> {
            if (!serviceMonthList.isEmpty() && currentMonthIndex > 0) {
                currentMonthIndex--;
                updateCalendarView();
            }
        });

        // ▶ 다음 달 버튼 클릭
        binding.btnNextMonth.setOnClickListener(v -> {
            if (!serviceMonthList.isEmpty() && currentMonthIndex < serviceMonthList.size() - 1) {
                currentMonthIndex++;
                updateCalendarView();
            }
        });

        registerDateChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTodayViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dateChangeReceiver != null) {
            unregisterReceiver(dateChangeReceiver);
        }
    }

    private void toggleScheduleSheet() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void showAddScheduleDialog() {
        DialogScheduleAddBinding dialogBinding = DialogScheduleAddBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);

        if (!serviceMonthList.isEmpty() && currentMonthIndex < serviceMonthList.size()) {
            String currentYM = serviceMonthList.get(currentMonthIndex);
            String[] parts = currentYM.split("-");
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
        }

        if (selectedDayNumber != null && !selectedDayNumber.isEmpty()) {
            try {
                day = Integer.parseInt(selectedDayNumber);
            } catch (Exception ignored) {}
        }

        String targetDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month, day);
        dialogBinding.tvAddScheduleDate.setText(String.format(Locale.KOREA, "%d년 %d월 %d일", year, month, day));

        dialogBinding.btnSaveSchedule.setOnClickListener(v -> {
            String title = dialogBinding.inputScheduleTitle.getText().toString().trim();
            String startTime = dialogBinding.inputStartTime.getText().toString().trim();
            String endTime = dialogBinding.inputEndTime.getText().toString().trim();
            String location = dialogBinding.inputLocation.getText().toString().trim();
            String memo = dialogBinding.inputScheduleMemo.getText().toString().trim();

            if (title.isEmpty()) {
                dialogBinding.inputScheduleTitle.setError("일정 제목을 입력해 주세요.");
                return;
            }

            ScheduleItem newItem = new ScheduleItem(targetDate, title, startTime, endTime, location, "개인", memo);
            allScheduleList.add(0, newItem);

            updateCalendarView();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEnlistmentDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String enlistDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "입대일: " + enlistDate, Toast.LENGTH_SHORT).show();
                    showDischargeDatePicker(year, month, enlistDate);
                },
                2025, 5, 16
        );
        dialog.setTitle("입대일을 선택하세요");
        dialog.show();
    }

    private void showDischargeDatePicker(int startYear, int startMonth, String enlistDate) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String dischargeDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "전역일: " + dischargeDate, Toast.LENGTH_SHORT).show();
                    generateServiceMonthList(startYear, startMonth, year, month);
                },
                startYear + 1, startMonth + 6, 15
        );
        dialog.setTitle("전역일을 선택하세요");
        dialog.show();
    }

    private void generateServiceMonthList(int startYear, int startMonth, int endYear, int endMonth) {
        serviceMonthList.clear();

        Calendar startCal = Calendar.getInstance();
        startCal.set(startYear, startMonth, 1);

        Calendar endCal = Calendar.getInstance();
        endCal.set(endYear, endMonth, 1);

        while (!startCal.after(endCal)) {
            int y = startCal.get(Calendar.YEAR);
            int m = startCal.get(Calendar.MONTH) + 1;
            String yearMonth = String.format(Locale.KOREA, "%04d-%02d", y, m);
            serviceMonthList.add(yearMonth);
            startCal.add(Calendar.MONTH, 1);
        }

        currentMonthIndex = 0;
        Toast.makeText(this, "총 복무 달수: " + serviceMonthList.size() + "개월 생성 완료!", Toast.LENGTH_SHORT).show();
        updateCalendarView();
    }

    private void updateCalendarView() {
        if (serviceMonthList.isEmpty()) return;

        String currentYM = serviceMonthList.get(currentMonthIndex);
        String[] parts = currentYM.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;

        Calendar realToday = Calendar.getInstance();
        int realYear = realToday.get(Calendar.YEAR);
        int realMonth = realToday.get(Calendar.MONTH);
        int realDay = realToday.get(Calendar.DAY_OF_MONTH);

        String todayDayStr = (year == realYear && month == realMonth) ? String.valueOf(realDay) : "";

        binding.tvYear.setText(year + "년");
        binding.tvMonth.setText((month + 1) + "월");

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<String> daysInMonth = new ArrayList<>();
        for (int i = 0; i < firstDayOfWeek; i++) {
            daysInMonth.add("");
        }
        for (int i = 1; i <= maxDays; i++) {
            daysInMonth.add(String.valueOf(i));
        }

        CalendarDayAdapter dayAdapter = new CalendarDayAdapter(daysInMonth, selectedDayNumber, todayDayStr, new CalendarDayAdapter.OnDayClickListener() {
            @Override
            public void onDaySelect(String day) {
                selectedDayNumber = day;
                updateCalendarView();
            }

            @Override
            public void onDayAddSchedule(String day) {
                String fullDateStr = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, Integer.parseInt(day));
                showAddScheduleDialogForDate(fullDateStr);
            }
        });

        binding.rvCalendarDays.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        binding.rvCalendarDays.setAdapter(dayAdapter);

        String targetDateStr = "";
        if (selectedDayNumber != null && !selectedDayNumber.isEmpty()) {
            try {
                int dayInt = Integer.parseInt(selectedDayNumber);
                targetDateStr = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayInt);
            } catch (Exception ignored) {}
        }

        if (scheduleAdapter != null) {
            scheduleAdapter.setCalendarContext(targetDateStr, currentYM);
        }

        List<ScheduleItem> monthSchedules = filterSchedules(year, month, targetDateStr);
        scheduleList.clear();
        scheduleList.addAll(monthSchedules);

        if (scheduleAdapter != null) {
            scheduleAdapter.notifyDataSetChanged();
        }
    }

    private void setupTabListeners() {
        binding.scheduleSheet.tabThisMonth.setOnClickListener(v -> applyFilter("THIS_MONTH"));
        binding.scheduleSheet.tabUnit.setOnClickListener(v -> applyFilter("UNIT"));
        binding.scheduleSheet.tabVacation.setOnClickListener(v -> applyFilter("VACATION"));
        binding.scheduleSheet.tabPersonal.setOnClickListener(v -> applyFilter("PERSONAL"));

        updateTabStyles();
    }

    private void applyFilter(String filter) {
        this.currentFilter = filter;
        updateTabStyles();
        updateCalendarView();
    }

    private void initDummySchedules() {
        allScheduleList.clear();
        allScheduleList.add(new ScheduleItem("2026-10-01", "국군의 날 행사", "10:00", "12:00", "연병장", "부대", "정복 착용"));
        allScheduleList.add(new ScheduleItem("2026-10-09", "한글날 위로휴가", "00:00", "23:59", "자택", "휴가", "휴가증 지참"));
        allScheduleList.add(new ScheduleItem("2026-11-15", "동계 진지공사", "08:00", "17:00", "진지 A", "부대", "방한장갑 준비"));
        allScheduleList.add(new ScheduleItem("2026-12-25", "크리스마스 특별점호", "20:00", "21:00", "생활관", "개인", "특식 지급"));
    }

    private void showAddScheduleDialogForDate(String targetDate) {
        DialogScheduleAddBinding dialogBinding = DialogScheduleAddBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogBinding.tvAddScheduleDate.setText(targetDate);

        dialogBinding.btnSaveSchedule.setOnClickListener(v -> {
            String title = dialogBinding.inputScheduleTitle.getText().toString().trim();
            String startTime = dialogBinding.inputStartTime.getText().toString().trim();
            String endTime = dialogBinding.inputEndTime.getText().toString().trim();
            String location = dialogBinding.inputLocation.getText().toString().trim();
            String memo = dialogBinding.inputScheduleMemo.getText().toString().trim();

            if (title.isEmpty()) {
                dialogBinding.inputScheduleTitle.setError("일정 제목을 입력해 주세요.");
                return;
            }

            ScheduleItem newItem = new ScheduleItem(targetDate, title, startTime, endTime, location, "개인", memo);
            allScheduleList.add(0, newItem);

            updateCalendarView();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showScheduleDetailDialog(ScheduleItem item) {
        if (item == null) return;

        DialogScheduleDetailBinding dialogBinding = DialogScheduleDetailBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        String categoryText = (item.getType() != null) ? item.getType() + " 일정" : "일정";
        dialogBinding.tvDetailCategory.setText(categoryText);
        dialogBinding.tvDetailTitle.setText(item.getTitle());

        String startTimeStr = (item.getStartTime() != null && !item.getStartTime().isEmpty()) ? item.getStartTime() : "-";
        String endTimeStr = (item.getEndTime() != null && !item.getEndTime().isEmpty()) ? item.getEndTime() : "-";
        String locationStr = (item.getLocation() != null && !item.getLocation().isEmpty()) ? item.getLocation() : "-";

        dialogBinding.tvDetailStart.setText("시작 시간\n" + startTimeStr);
        dialogBinding.tvDetailEnd.setText("종료 시간\n" + endTimeStr);
        dialogBinding.tvDetailLocation.setText("장소\n" + locationStr);

        if (item.getDate() != null && !item.getDate().isEmpty()) {
            dialogBinding.tvFixedMessage.setVisibility(View.VISIBLE);
            dialogBinding.tvFixedMessage.setText("일정 일자: " + item.getDate());
        } else {
            dialogBinding.tvFixedMessage.setVisibility(View.GONE);
        }

        dialogBinding.inputDetailMemo.setText(item.getMemo());

        dialogBinding.btnCloseDetail.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSaveMemo.setOnClickListener(v -> {
            String newMemo = dialogBinding.inputDetailMemo.getText().toString().trim();
            item.setMemo(newMemo);
            Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            updateCalendarView();
            dialog.dismiss();
        });

        dialogBinding.btnDeleteSchedule.setOnClickListener(v -> {
            allScheduleList.remove(item);
            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            updateCalendarView();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void moveToToday() {
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        String targetYM = String.format(Locale.KOREA, "%04d-%02d", todayYear, todayMonth + 1);

        int foundIndex = -1;
        for (int i = 0; i < serviceMonthList.size(); i++) {
            if (serviceMonthList.get(i).equals(targetYM)) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1) {
            currentMonthIndex = foundIndex;
            selectedDayNumber = String.valueOf(todayDay);
            updateCalendarView();
        } else {
            Toast.makeText(this, "오늘 날짜가 복무 기간에 포함되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private List<ScheduleItem> filterSchedules(int year, int month, String targetDateStr) {
        List<ScheduleItem> selectedSchedules = new ArrayList<>();
        List<ScheduleItem> monthSchedules = new ArrayList<>();
        List<ScheduleItem> futureSchedules = new ArrayList<>();
        List<ScheduleItem> pastSchedules = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        String todayStr = String.format(Locale.KOREA, "%04d-%02d-%02d",
                today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));
        String currentYM = String.format(Locale.KOREA, "%04d-%02d", year, month + 1);

        String priorityDate = (targetDateStr != null && !targetDateStr.isEmpty()) ? targetDateStr : todayStr;

        for (ScheduleItem item : allScheduleList) {
            if (item.getDate() == null) continue;

            String type = (item.getType() != null) ? item.getType() : "";
            boolean matchesType = "THIS_MONTH".equals(currentFilter) ||
                    ("UNIT".equals(currentFilter) && "부대".equals(type)) ||
                    ("VACATION".equals(currentFilter) && "휴가".equals(type)) ||
                    ("PERSONAL".equals(currentFilter) && "개인".equals(type));

            if (!matchesType) continue;

            if (item.getDate().equals(priorityDate)) {
                selectedSchedules.add(item);
            } else if (item.getDate().startsWith(currentYM)) {
                monthSchedules.add(item);
            } else if (item.getDate().compareTo(currentYM + "-31") > 0) {
                futureSchedules.add(item);
            } else {
                pastSchedules.add(item);
            }
        }

        List<ScheduleItem> result = new ArrayList<>();
        result.addAll(selectedSchedules);
        result.addAll(monthSchedules);
        result.addAll(futureSchedules);
        result.addAll(pastSchedules);

        return result;
    }

    private void updateTabStyles() {
        binding.scheduleSheet.tabThisMonth.setSelected("THIS_MONTH".equals(currentFilter));
        binding.scheduleSheet.tabUnit.setSelected("UNIT".equals(currentFilter));
        binding.scheduleSheet.tabVacation.setSelected("VACATION".equals(currentFilter));
        binding.scheduleSheet.tabPersonal.setSelected("PERSONAL".equals(currentFilter));
    }

    private void initTodayViews() {
        Calendar today = Calendar.getInstance();
        int todayMonth = today.get(Calendar.MONTH) + 1;
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        binding.tvTodayMonth.setText(todayMonth + "월");
        binding.tvTodayNumber.setText(String.valueOf(todayDay));
        binding.scheduleSheet.tabThisMonth.setText("전체");
    }

    private void registerDateChangeReceiver() {
        android.content.IntentFilter filter = new android.content.IntentFilter();
        filter.addAction(android.content.Intent.ACTION_DATE_CHANGED);
        filter.addAction(android.content.Intent.ACTION_TIME_TICK);

        dateChangeReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, android.content.Intent intent) {
                initTodayViews();
                updateCalendarView();
            }
        };

        registerReceiver(dateChangeReceiver, filter);
    }
}
