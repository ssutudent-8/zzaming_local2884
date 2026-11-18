package com.example.c20232884;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.app.DatePickerDialog; // DatePickerDialog import
import android.widget.Toast; // Toast import


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

    private String currentFilter = "THIS_MONTH";

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



        //일정 시트 확장
        bottomSheetBehavior = BottomSheetBehavior.from(binding.scheduleSheet.getRoot());
        bottomSheetBehavior.setFitToContents(true);

        int peekHeightPx = (int)(300 * getResources().getDisplayMetrics().density);
        bottomSheetBehavior.setPeekHeight(peekHeightPx);

        binding.scheduleSheet.btnScheduleHandle.setOnClickListener(v -> toggleScheduleSheet());

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.scheduleSheet.btnAddSchedule.setVisibility(View.VISIBLE);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.dimOverlay.setVisibility(View.GONE);
                    binding.scheduleSheet.btnAddSchedule.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // slideOffset: 0.0 (접힘) ~ 1.0 (최대 확장)
                // 손가락으로 올리고 내리는 위치 비율에 따라 배경 투명도(Alpha) 실시간 변화

                if (slideOffset > 0) {
                    binding.dimOverlay.setVisibility(View.VISIBLE);
                    binding.dimOverlay.setAlpha(slideOffset); // 손가락 움직임에 맞춰 자연스럽게 어두워짐
                }
            }
        });

        binding.dimOverlay.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.btnBack.setOnClickListener(v -> finish());

        //일정 시트 확장 끝


        //버튼으로 일정 추가
        binding.scheduleSheet.btnAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        // 1. 더미 데이터 초기화
        initDummySchedules();

        // 2. 달력 시계 초기화
        currentCalendar = Calendar.getInstance();

        // 3. 탭 클릭 이벤트 연결
        setupTabListeners();

        // 4. 어댑터 연결
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        binding.scheduleSheet.rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.scheduleSheet.rvSchedules.setAdapter(scheduleAdapter);

        // ◀ 이전 달 버튼 (‹) 클릭
        binding.btnPreviousMonth.setOnClickListener(v -> {
            if (!serviceMonthList.isEmpty() && currentMonthIndex > 0) {
                currentMonthIndex--;
                updateCalendarView();
            }
        });

        // ▶ 다음 달 버튼 (›) 클릭
        binding.btnNextMonth.setOnClickListener(v -> {
            if (!serviceMonthList.isEmpty() && currentMonthIndex < serviceMonthList.size() - 1) {
                currentMonthIndex++;
                updateCalendarView();
            }
        });

    }

    private void toggleScheduleSheet() {
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
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

        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogBinding.tvAddScheduleDate.setText("2025년 6월 16일");

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

            ScheduleItem newItem = new ScheduleItem("2025-06-16", title, startTime, endTime, location, "개인", memo);
            scheduleList.add(0, newItem);

            if (binding.scheduleSheet.rvSchedules.getAdapter() != null) {
                binding.scheduleSheet.rvSchedules.getAdapter().notifyItemInserted(0);
                binding.scheduleSheet.rvSchedules.scrollToPosition(0);
            }

            dialog.dismiss();
        });

        dialog.show();

    }

    private List<ScheduleItem> getSchedulesForMonth(int year, int month) {
        List<ScheduleItem> filteredList = new ArrayList<>();
        String targetYearMonth = String.format(Locale.KOREA, "%04d-%02d", year, month + 1);

        for (ScheduleItem item : allScheduleList) {
            if (item.getDate() != null && item.getDate().startsWith(targetYearMonth)) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    /**
     * 1차: 입대일 선택 기본 다이얼로그
     */
    private void showEnlistmentDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String enlistDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "입대일: " + enlistDate, Toast.LENGTH_SHORT).show();

                    // 입대일 선택 완료 후 ➔ 2차: 전역일 선택 다이얼로그 연달아 띄우기
                    showDischargeDatePicker(year, month, enlistDate);
                },
                2025, 5, 16 // 기본 보여줄 날짜 (2025년 6월 16일)
        );
        dialog.setTitle("입대일을 선택하세요");
        dialog.show();
    }

    /**
     * 2차: 전역일 선택 기본 다이얼로그
     */
    private void showDischargeDatePicker(int startYear, int startMonth, String enlistDate) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String dischargeDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "전역일: " + dischargeDate, Toast.LENGTH_SHORT).show();

                    //[2단계 연결] 입대월~전역월 모든 연/월 리스트 자동 생성
                    generateServiceMonthList(startYear, startMonth, year, month);
                },
                startYear + 1, startMonth + 6, 15 // 기본 전역일 추정값 (1년 6개월 뒤)
        );
        dialog.setTitle("전역일을 선택하세요");
        dialog.show();
    }

    /**
     * [2단계] 입대월부터 전역월까지의 모든 연/월("2025-06") 리스트를 생성하는 함수
     */
    private void generateServiceMonthList(int startYear, int startMonth, int endYear, int endMonth) {
        serviceMonthList.clear();

        Calendar startCal = Calendar.getInstance();
        startCal.set(startYear, startMonth, 1);

        Calendar endCal = Calendar.getInstance();
        endCal.set(endYear, endMonth, 1);

        // 입대월부터 전역월까지 1달씩 늘리면서 리스트에 추가
        while (!startCal.after(endCal)) {
            int y = startCal.get(Calendar.YEAR);
            int m = startCal.get(Calendar.MONTH) + 1; // 0이 1월이므로 +1
            String yearMonth = String.format(Locale.KOREA, "%04d-%02d", y, m);
            serviceMonthList.add(yearMonth);

            startCal.add(Calendar.MONTH, 1); // 1달씩 증가
        }

        currentMonthIndex = 0; // 입대월(첫 번째 달)부터 시작
        Toast.makeText(this, "총 복무 달수: " + serviceMonthList.size() + "개월 생성 완료!", Toast.LENGTH_SHORT).show();

        updateCalendarView();
    }

    /**
     * [2단계 통합 함수] 현재 선택된 복무 연/월(serviceMonthList)에 맞춰
     * 헤더 텍스트 + 달력 그리드 일수/요일 + 일정 목록 3가지를 동시에 동기화 갱신하는 함수
     */
    private void updateCalendarView() {
        if (serviceMonthList.isEmpty()) return;

        // 1. 현재 선택된 연/월 문자열 가져오기 (예: "2026-10")
        String currentYM = serviceMonthList.get(currentMonthIndex);
        String[] parts = currentYM.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1; // 자바 Calendar는 0이 1월이므로 -1

        // [동기화 1] 상단 연/월 헤더 텍스트 동적 변경
        binding.tvYear.setText(year + "년");
        binding.tvMonth.setText((month + 1) + "월");

        // [동기화 2] 1일이 시작하는 요일 및 해당 월의 마지막 일수(28~31일) 자동 계산
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1; // 1일의 요일 (0=일요일, 1=월요일...)
        int maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH); // 그 달의 총 일수 (28, 30, 31 등)

        List<String> daysInMonth = new ArrayList<>();
        // 1일 시작 전 빈 칸("" ) 채우기
        for (int i = 0; i < firstDayOfWeek; i++) {
            daysInMonth.add("");
        }
        // 1일부터 실제 마지막 날짜까지 숫자 채우기
        for (int i = 1; i <= maxDays; i++) {
            daysInMonth.add(String.valueOf(i));
        }

        // 날짜 클릭 시 해당 날짜로 일정 추가 다이얼로그 연동
        CalendarDayAdapter dayAdapter = new CalendarDayAdapter(daysInMonth, selectedDay -> {
            if (serviceMonthList.isEmpty()) return;

            String fullDateStr = currentYM + "-" + String.format(Locale.KOREA, "%02d", Integer.parseInt(selectedDay));

            // 클릭한 동적 날짜로 다이얼로그 팝업 호출
            showAddScheduleDialogForDate(fullDateStr);
        });

        binding.rvCalendarDays.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        binding.rvCalendarDays.setAdapter(dayAdapter);

        // [동기화 3] 이번 달에 해당하는 일정만 스마트 필터링 후 리스트 갱신
        List<ScheduleItem> newMonthSchedules = getSchedulesForMonth(year, month);
        scheduleList.clear();
        scheduleList.addAll(newMonthSchedules);

        if (binding.scheduleSheet.rvSchedules.getAdapter() != null) {
            binding.scheduleSheet.rvSchedules.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 일정 분류 탭 클릭 이벤트 등록 메서드
     */
    private void setupTabListeners() {
        binding.scheduleSheet.tabThisMonth.setOnClickListener(v -> applyFilter("THIS_MONTH"));
        binding.scheduleSheet.tabUnit.setOnClickListener(v -> applyFilter("UNIT"));
        binding.scheduleSheet.tabVacation.setOnClickListener(v -> applyFilter("VACATION"));
        binding.scheduleSheet.tabPersonal.setOnClickListener(v -> applyFilter("PERSONAL"));
        binding.scheduleSheet.tabAll.setOnClickListener(v -> applyFilter("ALL"));
    }

    /**
     * 카테고리 필터 변경 및 화면 갱신 메서드
     */
    private void applyFilter(String filter) {
        this.currentFilter = filter;
        updateCalendarView();
    }

    /**
     * 초기 테스트용 마스터 더미 일정 생성 메서드
     */
    private void initDummySchedules() {
        allScheduleList.clear();
        allScheduleList.add(new ScheduleItem("2026-10-01", "국군의 날 행사", "10:00", "12:00", "연병장", "부대", "정복 착용"));
        allScheduleList.add(new ScheduleItem("2026-10-09", "한글날 위로휴가", "00:00", "23:59", "자택", "휴가", "휴가증 지참"));
        allScheduleList.add(new ScheduleItem("2026-11-15", "동계 진지공사", "08:00", "17:00", "진지 A", "부대", "방한장갑 준비"));
        allScheduleList.add(new ScheduleItem("2026-12-25", "크리스마스 특별점호", "20:00", "21:00", "생활관", "개인", "특식 지급"));
    }

    /**
     * 터치한 특정 날짜(예: "2026-10-18")로 일정 추가 다이얼로그 호출 메서드
     */
    private void showAddScheduleDialogForDate(String targetDate) {
        DialogScheduleAddBinding dialogBinding = DialogScheduleAddBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 클릭한 날짜 텍스트 세팅
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

            // 클릭된 특정 날짜로 일정 생성
            ScheduleItem newItem = new ScheduleItem(targetDate, title, startTime, endTime, location, "개인", memo);
            allScheduleList.add(0, newItem);

            updateCalendarView();
            dialog.dismiss();
        });

        dialog.show();
    }

}