package com.example.c20232884;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.c20232884.databinding.ActivityMainBinding;
import com.example.c20232884.databinding.CardScheduleBinding;
import com.example.c20232884.model.ScheduleItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String memberType = "병사";
    private int startYear = 2025;
    private int startMonth = 5; // 6월 (0이 1월)
    private int endYear = 2026;
    private int endMonth = 11; // 12월 (0이 1월)

    private final String[] weekDates = new String[7]; // "YYYY-MM-DD"
    private final List<ScheduleItem> unitSchedules = new ArrayList<>();
    private String selectedFullDate = "";
    private String todayFullDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 더미 서버 및 SharedPreferences 영구 저장소 초기화
        MockServerDataSource.getInstance(getApplicationContext());
        if (MockServerDataSource.getInstance().getCurrentUser() != null) {
            memberType = MockServerDataSource.getInstance().getCurrentUser().getMemberType();
        }

        // 복무 및 권한 설정 버튼
        binding.btnMainSetting.setOnClickListener(v -> showEnlistmentDatePicker());

        // 1. 주간 캘린더 카드 위젯 초기화 및 이벤트 연결
        setupWeeklyScheduleWidget();

        // 2. 부대 게시판 이동 카드 연결
        binding.includeCardBoard.cardBoard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            intent.putExtra("memberType", memberType);
            startActivity(intent);
        });

        // 3. 마음의 편지함 이동 카드 연결
        binding.includeCardLetter.cardLetter.setOnClickListener(v -> handleLetterCardClick());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MockServerDataSource.getInstance().getCurrentUser() != null) {
            memberType = MockServerDataSource.getInstance().getCurrentUser().getMemberType();
        }
        updateSubTextCards();
        loadWeeklySchedules();
    }

    private void updateSubTextCards() {
        if ("간부".equals(memberType)) {
            binding.includeCardLetter.tvLetterSub.setText("대원 무기명 건의 및 수신함 진입");
        } else {
            binding.includeCardLetter.tvLetterSub.setText("부대 개선을 위한 익명 마음의 편지 작성");
        }
    }

    private void setupWeeklyScheduleWidget() {
        CardScheduleBinding scheduleBinding = binding.includeCardSchedule;

        // 전체보기 버튼 클릭 시만 CalendarActivity로 이동
        scheduleBinding.btnCalendarAll.setOnClickListener(v -> openCalendarActivity());

        // 오늘 기준 주간(월~일) 날짜 계산
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diffToMon = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        cal.add(Calendar.DAY_OF_MONTH, diffToMon);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        todayFullDate = sdf.format(new Date());
        selectedFullDate = todayFullDate;

        LinearLayout[] dayCells = new LinearLayout[]{
                scheduleBinding.weekMon, scheduleBinding.weekTue, scheduleBinding.weekWed,
                scheduleBinding.weekThu, scheduleBinding.weekFri, scheduleBinding.weekSat, scheduleBinding.weekSun
        };

        TextView[] labelViews = new TextView[]{
                scheduleBinding.tvWeekMonLabel, scheduleBinding.tvWeekTueLabel, scheduleBinding.tvWeekWedLabel,
                scheduleBinding.tvWeekThuLabel, scheduleBinding.tvWeekFriLabel, scheduleBinding.tvWeekSatLabel, scheduleBinding.tvWeekSunLabel
        };

        TextView[] dateViews = new TextView[]{
                scheduleBinding.tvWeekMonDate, scheduleBinding.tvWeekTueDate, scheduleBinding.tvWeekWedDate,
                scheduleBinding.tvWeekThuDate, scheduleBinding.tvWeekFriDate, scheduleBinding.tvWeekSatDate, scheduleBinding.tvWeekSunDate
        };

        for (int i = 0; i < 7; i++) {
            weekDates[i] = sdf.format(cal.getTime());
            int dateNum = cal.get(Calendar.DAY_OF_MONTH);
            dateViews[i].setText(String.valueOf(dateNum));

            final int dayIndex = i;
            dayCells[i].setOnClickListener(v -> {
                selectedFullDate = weekDates[dayIndex];
                updateSelectedDayHighlight(dayCells, labelViews, dateViews, dayIndex);
                displayScheduleForSelectedDate();
            });

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // +n개 일정 버튼 클릭 시 캘린더 해당 날짜 및 '부대' 탭 자동 선택 이동
        scheduleBinding.tvTodayScheduleMore.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            intent.putExtra("memberType", memberType);
            intent.putExtra("startYear", startYear);
            intent.putExtra("startMonth", startMonth);
            intent.putExtra("endYear", endYear);
            intent.putExtra("endMonth", endMonth);
            intent.putExtra("targetDate", selectedFullDate);
            intent.putExtra("targetFilter", "UNIT");
            startActivity(intent);
        });

        int todayIndex = 0;
        for (int i = 0; i < 7; i++) {
            if (weekDates[i].equals(todayFullDate)) {
                todayIndex = i;
                break;
            }
        }
        updateSelectedDayHighlight(dayCells, labelViews, dateViews, todayIndex);
        loadWeeklySchedules();
    }

    private void updateSelectedDayHighlight(LinearLayout[] dayCells, TextView[] labelViews, TextView[] dateViews, int selectedIndex) {
        for (int i = 0; i < 7; i++) {
            boolean isSelected = (i == selectedIndex);
            boolean isToday = weekDates[i].equals(todayFullDate);

            if (isToday && isSelected) {
                // 오늘이면서 선택된 날짜: 오늘 테두리 상자(bg_day_today) + 어두운 초록 글자
                dayCells[i].setBackgroundResource(R.drawable.bg_day_today);
                labelViews[i].setTextColor(ContextCompat.getColor(this, R.color.forest_700));
                dateViews[i].setTextColor(ContextCompat.getColor(this, R.color.forest_900));
            } else if (isSelected) {
                // 오늘이 아니지만 클릭하여 선택된 날짜: 연한 연두색 채움(bg_day_selected) + 어두운 글자
                dayCells[i].setBackgroundResource(R.drawable.bg_day_selected);
                labelViews[i].setTextColor(ContextCompat.getColor(this, R.color.forest_700));
                dateViews[i].setTextColor(ContextCompat.getColor(this, R.color.forest_900));
            } else if (isToday) {
                // 오늘이지만 다른 날짜가 선택된 상태: 오늘 테두리 상자(bg_day_today) 유효 표출
                dayCells[i].setBackgroundResource(R.drawable.bg_day_today);
                labelViews[i].setTextColor(ContextCompat.getColor(this, R.color.forest_700));
                dateViews[i].setTextColor(ContextCompat.getColor(this, R.color.forest_700));
            } else {
                // 일반 비선택 날짜: 배경 없음, 기본 글자색
                dayCells[i].setBackground(null);
                labelViews[i].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                dateViews[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
        }
    }

    private void loadWeeklySchedules() {
        MockServerDataSource.getInstance(this).fetchServerSchedules(new MockServerDataSource.OnScheduleLoadListener() {
            @Override
            public void onSuccess(List<ScheduleItem> scheduleList) {
                unitSchedules.clear();
                if (scheduleList != null) {
                    unitSchedules.addAll(scheduleList);
                }
                updateEventDots();
                displayScheduleForSelectedDate();
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void updateEventDots() {
        CardScheduleBinding scheduleBinding = binding.includeCardSchedule;
        View[] dots1 = new View[]{
                scheduleBinding.dotWeekMon1, scheduleBinding.dotWeekTue1, scheduleBinding.dotWeekWed1,
                scheduleBinding.dotWeekThu1, scheduleBinding.dotWeekFri1, scheduleBinding.dotWeekSat1, scheduleBinding.dotWeekSun1
        };
        View[] dots2 = new View[]{
                scheduleBinding.dotWeekMon2, scheduleBinding.dotWeekTue2, scheduleBinding.dotWeekWed2,
                scheduleBinding.dotWeekThu2, scheduleBinding.dotWeekFri2, scheduleBinding.dotWeekSat2, scheduleBinding.dotWeekSun2
        };
        View[] dots3 = new View[]{
                scheduleBinding.dotWeekMon3, scheduleBinding.dotWeekTue3, scheduleBinding.dotWeekWed3,
                scheduleBinding.dotWeekThu3, scheduleBinding.dotWeekFri3, scheduleBinding.dotWeekSat3, scheduleBinding.dotWeekSun3
        };

        int unitColor = ContextCompat.getColor(this, R.color.forest_500);
        int personalColor = ContextCompat.getColor(this, R.color.personal);

        for (int i = 0; i < 7; i++) {
            String dateStr = weekDates[i];
            List<Integer> dotColorList = new ArrayList<>();

            if (dateStr != null) {
                List<Integer> unitList = new ArrayList<>();
                List<Integer> personalList = new ArrayList<>();

                for (ScheduleItem item : unitSchedules) {
                    if (dateStr.equals(item.getDate())) {
                        if ("부대".equals(item.getType())) {
                            unitList.add(unitColor);
                        } else {
                            personalList.add(personalColor);
                        }
                    }
                }
                // 부대 일정(초록점) 우선 배치 후 개인/휴가 일정(빨간점) 배치
                dotColorList.addAll(unitList);
                dotColorList.addAll(personalList);
            }

            if (dotColorList.size() >= 1) {
                dots1[i].setVisibility(View.VISIBLE);
                dots1[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColorList.get(0)));
            } else {
                dots1[i].setVisibility(View.INVISIBLE);
            }

            if (dotColorList.size() >= 2) {
                dots2[i].setVisibility(View.VISIBLE);
                dots2[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColorList.get(1)));
            } else {
                dots2[i].setVisibility(View.INVISIBLE);
            }

            if (dotColorList.size() >= 3) {
                dots3[i].setVisibility(View.VISIBLE);
                dots3[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColorList.get(2)));
            } else {
                dots3[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void displayScheduleForSelectedDate() {
        CardScheduleBinding scheduleBinding = binding.includeCardSchedule;
        List<ScheduleItem> matchedItems = new ArrayList<>();

        for (ScheduleItem item : unitSchedules) {
            if (selectedFullDate != null && selectedFullDate.equals(item.getDate())) {
                matchedItems.add(item);
            }
        }

        if (!matchedItems.isEmpty()) {
            ScheduleItem firstItem = matchedItems.get(0);
            scheduleBinding.tvTodayScheduleTime.setText(firstItem.getTimeRange());
            scheduleBinding.tvTodayScheduleTitle.setText(firstItem.getTitle());

            String desc = (firstItem.getLocation() != null) ? firstItem.getLocation() : "";
            if (firstItem.getMemo() != null && !firstItem.getMemo().isEmpty()) {
                if (!desc.isEmpty()) desc += " · ";
                desc += firstItem.getMemo();
            }
            scheduleBinding.tvTodayScheduleDesc.setText(desc);

            if (matchedItems.size() > 1) {
                int extraCount = matchedItems.size() - 1;
                scheduleBinding.tvTodayScheduleMore.setText("+" + extraCount + "개 일정");
                scheduleBinding.tvTodayScheduleMore.setVisibility(View.VISIBLE);
            } else {
                scheduleBinding.tvTodayScheduleMore.setVisibility(View.GONE);
            }
        } else {
            scheduleBinding.tvTodayScheduleTime.setText("-");
            scheduleBinding.tvTodayScheduleTitle.setText("일정 없음");
            scheduleBinding.tvTodayScheduleDesc.setText("선택한 날짜에 예정된 일정이 없습니다.");
            scheduleBinding.tvTodayScheduleMore.setVisibility(View.GONE);
        }
    }

    private void openCalendarActivity() {
        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        intent.putExtra("memberType", memberType);
        intent.putExtra("startYear", startYear);
        intent.putExtra("startMonth", startMonth);
        intent.putExtra("endYear", endYear);
        intent.putExtra("endMonth", endMonth);
        startActivity(intent);
    }

    private void handleLetterCardClick() {
        if ("간부".equals(memberType)) {
            new AlertDialog.Builder(this)
                    .setTitle("마음의 편지 수신함 진입")
                    .setMessage("무기명 보안 수신함으로 이동하시겠습니까?\n(외부 유출 주의해 주시기 바랍니다)")
                    .setPositiveButton("진입", (dialog, which) -> {
                        Intent intent = new Intent(MainActivity.this, PostActivity.class);
                        intent.putExtra("memberType", memberType);
                        intent.putExtra("startCategory", "마음의 편지");
                        startActivity(intent);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("마음의 편지 작성")
                    .setMessage("부대 개선 및 고충을 위한 무기명 마음의 편지를 작성하시겠습니까?")
                    .setPositiveButton("작성", (dialog, which) -> {
                        Intent intent = new Intent(MainActivity.this, PostComposeActivity.class);
                        intent.putExtra("memberType", memberType);
                        startActivity(intent);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }
    }

    private void showEnlistmentDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    this.startYear = year;
                    this.startMonth = month;
                    String enlistDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "입대일: " + enlistDate, Toast.LENGTH_SHORT).show();
                    showDischargeDatePicker(year, month, enlistDate);
                },
                startYear, startMonth, 16
        );
        dialog.setTitle("입대일을 선택하세요");
        dialog.show();
    }

    private void showDischargeDatePicker(int enlistYear, int enlistMonth, String enlistDate) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    this.endYear = year;
                    this.endMonth = month;
                    String dischargeDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "전역일: " + dischargeDate, Toast.LENGTH_SHORT).show();
                    showMemberTypeSelectionDialog();
                },
                enlistYear + 1, enlistMonth + 6, 15
        );
        dialog.setTitle("전역일을 선택하세요");
        dialog.show();
    }

    private void showMemberTypeSelectionDialog() {
        String[] options = {"병사", "간부"};
        final int[] selectedChoice = {"간부".equals(memberType) ? 1 : 0};

        new AlertDialog.Builder(this)
                .setTitle("사용자 유형을 선택하세요")
                .setSingleChoiceItems(options, selectedChoice[0], (dialog, which) -> {
                    selectedChoice[0] = which;
                })
                .setPositiveButton("확인", (dialog, which) -> {
                    memberType = options[selectedChoice[0]];
                    MockServerDataSource.getInstance().selectMemberType(memberType);
                    Toast.makeText(this, "유저 타입: " + memberType + "(으)로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    updateSubTextCards();
                    dialog.dismiss();
                })
                .setNegativeButton("취소", null)
                .setCancelable(false)
                .show();
    }
}
