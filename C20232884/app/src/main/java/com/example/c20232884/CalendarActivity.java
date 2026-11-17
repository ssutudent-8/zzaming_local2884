package com.example.c20232884;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;

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

import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {

    private ActivityCalendarBinding binding;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private List<ScheduleItem> scheduleList = new ArrayList<>();
    private List<ScheduleItem> allScheduleList = new ArrayList<>();
    private Calendar currentCalendar;
    private ScheduleAdapter scheduleAdapter;

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



        binding.tvYear.setText("2025년");
        binding.tvMonth.setText("6월");



        List<String> daysInMonth = new ArrayList<>();

        // 6월 1일은 일요일(빈칸 0개)부터 30일까지

        // 6월 1일은 일요일(빈칸 0개)부터 30일까지
        for (int i = 1; i <= 30; i++) {
            daysInMonth.add(String.valueOf(i));
        }


        CalendarDayAdapter dayAdapter = new CalendarDayAdapter(daysInMonth);

        binding.rvCalendarDays.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        binding.rvCalendarDays.setAdapter(dayAdapter);



        binding.btnBack.setOnClickListener(v -> finish());



        binding.btnBack.setOnClickListener(v -> finish());


        //더미데이터
        allScheduleList.add(new ScheduleItem("2026-10-01", "국군의 날 행사", "10:00",
                "12:00", "연병장", "부대", "정복 착용"));
        allScheduleList.add(new ScheduleItem("2026-10-09", "한글날 위로휴가", "00:00",
                "23:59", "자택", "휴가", "휴가증 지참"));
        allScheduleList.add(new ScheduleItem("2026-11-15", "동계 진지공사", "08:00",
                "17:00", "진지 A", "부대", "방한장갑 준비"));
        allScheduleList.add(new ScheduleItem("2026-12-25", "크리스마스 특별점호", "20:00",
                "21:00", "생활관", "개인", "특식 지급"));

        //더미데이터

        scheduleList = getSchedulesForMonth(2026, 9); // 9는 10월


        ScheduleAdapter adapter = new ScheduleAdapter(scheduleList);
        binding.scheduleSheet.rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.scheduleSheet.rvSchedules.setAdapter(adapter);

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

}