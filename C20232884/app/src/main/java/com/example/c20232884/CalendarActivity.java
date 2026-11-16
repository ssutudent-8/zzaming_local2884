package com.example.c20232884;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.c20232884.databinding.ActivityCalendarBinding;
import com.example.c20232884.model.ScheduleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {

    private ActivityCalendarBinding binding;
    private List<ScheduleItem> scheduleList;

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

        binding.tvYear.setText("2025년");
        binding.tvMonth.setText("6월");



        List<String> daysInMonth = new ArrayList<>();

        // 6월 1일은 일요일(빈칸 0개)부터 30일까지
        for (int i = 1; i <= 30; i++) {
            daysInMonth.add(String.valueOf(i));
        }

        CalendarDayAdapter dayAdapter = new CalendarDayAdapter(daysInMonth);

        binding.rvCalendarDays.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        binding.rvCalendarDays.setAdapter(dayAdapter);



        binding.btnBack.setOnClickListener(v -> finish());

        scheduleList = new ArrayList<>();

        scheduleList.add(new ScheduleItem(
                "2025-06-16",
                "개인 운동",
                "19:00",
                "20:00",
                "체력단련장",
                "개인",
                "스쿼트 5세트 하기"
        ));

        scheduleList.add(new ScheduleItem(
                "2025-08-15",
                "광복절 국기게양식",
                "08:00",
                "09:30",
                "연병장",
                "부대",
                "전투조끼 장구류 제거"
        ));

        ScheduleAdapter adapter = new ScheduleAdapter(scheduleList);
        binding.scheduleSheet.rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.scheduleSheet.rvSchedules.setAdapter(adapter);

    }
}