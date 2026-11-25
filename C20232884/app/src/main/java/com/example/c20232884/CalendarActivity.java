package com.example.c20232884;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

/**
 * 입대/전역일 복무 달력 동적 계산, 타임라인 일정 재배열 및 유저 권한별 상세 관리를 담당하는 메인 액티비티 클래스
 */
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
    private String selectedFullDate = "";
    private String currentFilter = "THIS_MONTH";
    private android.content.BroadcastReceiver dateChangeReceiver;

    // 유저 권한 및 복무 기간 타임스탬프 변수
    private String memberType = "병사"; // 기본값: "병사" ("병사" 또는 "간부")
    private long enlistmentMillis = 0;
    private long dischargeMillis = 0;

    // =============================================================================================
    // [그룹 1] 생명주기 및 기본 설정 메서드 (Lifecycle & Base Initialization)
    // =============================================================================================

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

        String passedMemberType = getIntent().getStringExtra("memberType");
        if (passedMemberType != null) {
            memberType = passedMemberType;
        }

        binding.btnYear.setOnClickListener(v -> toggleYearView());
        binding.btnToday.setOnClickListener(v -> moveToToday());

        // 일정 시트 높이 및 하단 마감 바 16dp 상쇄 패딩 세밀 설정
        bottomSheetBehavior = BottomSheetBehavior.from(binding.scheduleSheet.getRoot());
        bottomSheetBehavior.setFitToContents(true);

        int peekHeightPx = (int) (380 * getResources().getDisplayMetrics().density);
        bottomSheetBehavior.setPeekHeight(peekHeightPx);

        int calculatedPaddingPx = (int) (432 * getResources().getDisplayMetrics().density);
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

                    int calculatedPaddingPx = (int) (300 * getResources().getDisplayMetrics().density);
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

        // 1.5. 복무 기간 월 목록 자동 생성 (MainActivity에서 전달받은 값 또는 기본값 적용)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("startYear") && intent.hasExtra("endYear")) {
            int sYear = intent.getIntExtra("startYear", -1);
            int sMonth = intent.getIntExtra("startMonth", -1);
            int eYear = intent.getIntExtra("endYear", -1);
            int eMonth = intent.getIntExtra("endMonth", -1);

            if (sYear != -1 && eYear != -1) {
                generateServiceMonthList(sYear, sMonth, eYear, eMonth);
            }
        }

        // 2. 달력 시계 초기화
        currentCalendar = Calendar.getInstance();

        // 3. 탭 클릭 이벤트 연결
        setupTabListeners();
        initTodayViews();

        // 4. 어댑터 연결 및 클릭 리스너 설정
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        scheduleAdapter.setOnItemClickListener(new ScheduleAdapter.OnItemClickListener() {
            @Override
            public void onItemSelectDate(ScheduleItem item) {
                if (item != null && item.getDate() != null && item.getDate().contains("-")) {
                    String[] parts = item.getDate().split("-");
                    if (parts.length == 3) {
                        try {
                            int y = Integer.parseInt(parts[0]);
                            int m = Integer.parseInt(parts[1]);
                            int d = Integer.parseInt(parts[2]);

                            String itemYM = String.format(Locale.KOREA, "%04d-%02d", y, m);
                            selectedFullDate = String.format(Locale.KOREA, "%04d-%02d-%02d", y, m, d);
                            selectedDayNumber = String.valueOf(d);

                            int targetMonthIndex = serviceMonthList.indexOf(itemYM);
                            if (targetMonthIndex != -1) {
                                currentMonthIndex = targetMonthIndex; // [핵심] 해당 일정의 연/월로 월 즉시 이동
                            }
                            updateCalendarView(); // 100% 해당 월 이동 및 유일한 FullDate 선택 강조
                        } catch (Exception ignored) {}
                    }
                }
            }

            @Override
            public void onItemDetailClick(ScheduleItem item) {
                // 3점 버튼(⋮) 클릭 시: 상세정보 팝업 활성화
                showScheduleDetailDialog(item);
            }
        });
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

    /**
     * 자정 시점 시스템 날짜 변경 감지 브로드캐스트 리시버 등록 메서드
     */
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

    // =============================================================================================
    // [그룹 2] 복무 기간 & 유저 타입 설정 메서드 (Service Period & User Type Setup)
    // =============================================================================================

    /**
     * 1차: 입대일 선택 다이얼로그
     */
    private void showEnlistmentDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    enlistmentMillis = cal.getTimeInMillis();

                    String enlistDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "입대일: " + enlistDate, Toast.LENGTH_SHORT).show();
                    showDischargeDatePicker(year, month, enlistDate);
                },
                2025, 5, 16
        );
        dialog.setTitle("입대일을 선택하세요");
        dialog.show();
    }

    /**
     * 2차: 전역일 선택 다이얼로그
     */
    private void showDischargeDatePicker(int startYear, int startMonth, String enlistDate) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth, 23, 59, 59);
                    dischargeMillis = cal.getTimeInMillis();

                    String dischargeDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    Toast.makeText(this, "전역일: " + dischargeDate, Toast.LENGTH_SHORT).show();
                    generateServiceMonthList(startYear, startMonth, year, month);
                    showMemberTypeSelectionDialog();
                },
                startYear + 1, startMonth + 6, 15
        );
        dialog.setTitle("전역일을 선택하세요");
        dialog.show();
    }

    /**
     * 사용자 유형("병사" / "간부") 선택 다이얼로그
     */
    private void showMemberTypeSelectionDialog() {
        String[] options = {"병사", "간부"};
        new AlertDialog.Builder(this)
                .setTitle("사용자 유형을 선택하세요")
                .setSingleChoiceItems(options, "간부".equals(memberType) ? 1 : 0, (dialog, which) -> {
                    memberType = options[which];
                    Toast.makeText(this, "유저 타입: " + memberType + "(으)로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    updateCalendarView();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * '년' 버튼 클릭 시 축소된 복무 연도 전체 달력(year_view) 토글 전환 메서드
     */
    private void toggleYearView() {
        if (binding.yearView.getVisibility() == View.VISIBLE) {
            binding.yearView.setVisibility(View.GONE);
            binding.monthView.setVisibility(View.VISIBLE);
            binding.scheduleSheet.getRoot().setVisibility(View.VISIBLE);
            binding.btnYear.setSelected(false);
        } else {
            if (serviceMonthList == null || serviceMonthList.isEmpty()) {
                Toast.makeText(this, "입대일과 전역일을 먼저 설정해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.monthView.setVisibility(View.GONE);
            binding.scheduleSheet.getRoot().setVisibility(View.GONE);
            binding.yearView.setVisibility(View.VISIBLE);
            binding.btnYear.setSelected(true);

            setupYearViewAdapter();
        }
    }

    /**
     * 연도 전체 달력(year_view)에 2열 그리드 미니 월 카드 바인딩 메서드
     */
    private void setupYearViewAdapter() {
        binding.yearView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        binding.yearView.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
                com.example.c20232884.databinding.ItemYearMonthBinding monthBinding =
                        com.example.c20232884.databinding.ItemYearMonthBinding.inflate(getLayoutInflater(), parent, false);
                return new androidx.recyclerview.widget.RecyclerView.ViewHolder(monthBinding.getRoot()) {};
            }

            @Override
            public void onBindViewHolder(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
                com.example.c20232884.databinding.ItemYearMonthBinding monthBinding =
                        com.example.c20232884.databinding.ItemYearMonthBinding.bind(holder.itemView);

                String ym = serviceMonthList.get(position);
                String[] parts = ym.split("-");
                monthBinding.btnYearMonth.setText(parts[0] + "년 " + Integer.parseInt(parts[1]) + "월 ›");

                //카드 전체 및 내부 텍스트 뷰 양쪽에 클릭 이벤트를 연결하여 터치 반응성 극대화
                View.OnClickListener clickListener = v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        currentMonthIndex = pos;
                        toggleYearView();
                        updateCalendarView();
                    }
                };

                holder.itemView.setOnClickListener(clickListener);
                monthBinding.btnYearMonth.setOnClickListener(clickListener);
            }

            @Override
            public int getItemCount() {
                return (serviceMonthList != null) ? serviceMonthList.size() : 0;
            }
        });
    }

    /**
     * 입대월부터 전역월까지의 연/월 목록("2025-06") 자동 생성 메서드
     */
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

    // =============================================================================================
    // [그룹 3] 달력 그리드 동기화 & 탭 필터링 메서드 (Calendar Sync & Tab Filtering)
    // =============================================================================================

    /**
     * 현재 선택된 복무 연/월에 맞춰 헤더, 달력 그리드, 일자별 어댑터, 일정 목록 동시 동기화 메서드
     */
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
        String todayFullDateStr = String.format(Locale.KOREA, "%04d-%02d-%02d", realYear, realMonth + 1, realDay);

        // 1. 상단 연/월 헤더 텍스트 복원
        binding.tvYear.setText(year + "년");
        binding.tvMonth.setText((month + 1) + "월");

        // 2. 1일 시작 요일 및 해당 월 총 일수 자동 계산
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

        // 3. [복원] 달력 일자 그리드(rvCalendarDays) 어댑터 연결
        CalendarDayAdapter dayAdapter = new CalendarDayAdapter(
                daysInMonth, selectedFullDate, todayDayStr,
                allScheduleList, currentYM,
                new CalendarDayAdapter.OnDayClickListener() {
                    @Override
                    public void onDaySelect(String day) {
                        try {
                            int dayInt = Integer.parseInt(day);
                            selectedFullDate = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayInt);
                            selectedDayNumber = day;
                        } catch (Exception ignored) {}
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

        // 4. 오늘 기준 일정 4구역 고정 타임라인 세팅
        if (scheduleAdapter != null) {
            scheduleAdapter.setCalendarContext(todayFullDateStr, currentYM);
        }

        List<ScheduleItem> monthSchedules = filterSchedules(year, month);
        scheduleList.clear();
        scheduleList.addAll(monthSchedules);

        if (scheduleAdapter != null) {
            scheduleAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 일정 분류 탭 클릭 이벤트 연결 메서드
     */
    private void setupTabListeners() {
        binding.scheduleSheet.tabThisMonth.setOnClickListener(v -> applyFilter("THIS_MONTH"));
        binding.scheduleSheet.tabUnit.setOnClickListener(v -> applyFilter("UNIT"));
        binding.scheduleSheet.tabVacation.setOnClickListener(v -> applyFilter("VACATION"));
        binding.scheduleSheet.tabPersonal.setOnClickListener(v -> applyFilter("PERSONAL"));

        updateTabStyles();
    }

    /**
     * 카테고리 필터 변경 및 화면 재갱신 메서드
     */
    private void applyFilter(String filter) {
        this.currentFilter = filter;
        updateTabStyles();
        updateCalendarView();
    }

    /**
     * 선택된 탭 강조 스타일 정돈 메서드
     */
    private void updateTabStyles() {
        binding.scheduleSheet.tabThisMonth.setSelected("THIS_MONTH".equals(currentFilter));
        binding.scheduleSheet.tabUnit.setSelected("UNIT".equals(currentFilter));
        binding.scheduleSheet.tabVacation.setSelected("VACATION".equals(currentFilter));
        binding.scheduleSheet.tabPersonal.setSelected("PERSONAL".equals(currentFilter));
    }

    /**
     * 오늘 날짜 뷰 및 첫 탭("전체") 텍스트 고정 초기화 메서드
     */
    private void initTodayViews() {
        Calendar today = Calendar.getInstance();
        int todayMonth = today.get(Calendar.MONTH) + 1;
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        binding.tvTodayMonth.setText(todayMonth + "월");
        binding.tvTodayNumber.setText(String.valueOf(todayDay));
        binding.scheduleSheet.tabThisMonth.setText("전체");
    }

    /**
     * 오늘 날짜로 즉시 이동 메서드
     */
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
            // [수정] 오늘 버튼 클릭 시 해당 월 이동과 동시에 오늘 날짜(selectedFullDate)를 유일 선택 상태로 강조
            selectedFullDate = String.format(Locale.KOREA, "%04d-%02d-%02d", todayYear, todayMonth + 1, todayDay);
            selectedDayNumber = String.valueOf(todayDay);
            updateCalendarView();
        } else {
            Toast.makeText(this, "오늘 날짜가 복무 기간에 포함되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // =============================================================================================
    // [그룹 4] 일정 타임라인 분류 & 데이터 관리 메서드 (Schedule Timeline Sorting & Data)
    // =============================================================================================

    /**
     * 마스터 데이터(allScheduleList) 중 선택 날짜/오늘 일정을 최상단(1구역)에 두고 타임라인 4구역 순으로 재배열하는 메서드
     */
    private List<ScheduleItem> filterSchedules(int year, int month) {
        List<ScheduleItem> todaySchedules = new ArrayList<>();   // 1구역: 오늘 일정
        List<ScheduleItem> monthSchedules = new ArrayList<>();   // 2구역: 이번 달 남은 일정
        List<ScheduleItem> futureSchedules = new ArrayList<>();  // 3구역: 미래 일정
        List<ScheduleItem> pastSchedules = new ArrayList<>();    // 4구역: 지난 일정

        Calendar today = Calendar.getInstance();
        String todayStr = String.format(Locale.KOREA, "%04d-%02d-%02d",
                today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));
        String currentYM = String.format(Locale.KOREA, "%04d-%02d", year, month + 1);

        for (ScheduleItem item : allScheduleList) {
            if (item.getDate() == null) continue;

            String type = (item.getType() != null) ? item.getType() : "";
            boolean matchesType = "THIS_MONTH".equals(currentFilter) ||
                    ("UNIT".equals(currentFilter) && "부대".equals(type)) ||
                    ("VACATION".equals(currentFilter) && "휴가".equals(type)) ||
                    ("PERSONAL".equals(currentFilter) && "개인".equals(type));

            if (!matchesType) continue;

            // 항상 오늘 날짜(todayStr)를 기준으로 고정 분류
            if (item.getDate().equals(todayStr)) {
                todaySchedules.add(item);        // 1구역: 오늘 일정
            } else if (item.getDate().compareTo(todayStr) < 0) {
                pastSchedules.add(item);         // 4구역: 오늘 이전 지난 일정 (회색 처리)
            } else if (item.getDate().startsWith(currentYM)) {
                monthSchedules.add(item);        // 2구역: 이번 달 남은 일정
            } else {
                futureSchedules.add(item);       // 3구역: 미래 일정
            }
        }

        List<ScheduleItem> result = new ArrayList<>();
        result.addAll(todaySchedules);
        result.addAll(monthSchedules);
        result.addAll(futureSchedules);
        result.addAll(pastSchedules);

        return result;
    }

    /**
     * 초기 테스트용 마스터 더미 일정 생성 메서드
     */
    /**
     * 초기 테스트용 마스터 더미 일정 및 더미 서버(MockServerDataSource) 수신 메서드
     */
    private void initDummySchedules() {
        allScheduleList.clear();
        MockServerDataSource.getInstance(this).fetchServerUnitSchedules(new MockServerDataSource.OnScheduleLoadListener() {
            @Override
            public void onSuccess(List<ScheduleItem> scheduleList) {
                if (scheduleList != null) {
                    allScheduleList.addAll(scheduleList);
                }
                updateCalendarView();
            }

            @Override
            public void onFailure(Exception e) {}
        });

        // 2. 로컬 개인/휴가 일정 추가
        allScheduleList.add(new ScheduleItem("2026-10-09", "한글날 위로휴가", "00:00", "23:59", "자택", "휴가", "휴가증 지참"));
        allScheduleList.add(new ScheduleItem("2026-12-25", "크리스마스 특별점호", "20:00", "21:00", "생활관", "개인", "특식 지급"));
    }

    // =============================================================================================
    // [그룹 5] 다이얼로그 팝업 & 바텀시트 제어 메서드 (Dialogs & BottomSheet Control)
    // =============================================================================================

    /**
     * 일정 바텀시트 접기/펼치기 토글 메서드
     */
    private void toggleScheduleSheet() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /**
     * 하단 시트 '+' 버튼 클릭 시 기본 일정 추가 다이얼로그 호출 메서드
     */
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
        setupScheduleAddForm(dialogBinding, targetDate, dialog);
        dialog.show();
    }

    /**
     * 달력 날짜 2회 클릭 시 해당 날짜 지정 일정 추가 다이얼로그 호출 메서드
     */
    private void showAddScheduleDialogForDate(String targetDate) {
        DialogScheduleAddBinding dialogBinding = DialogScheduleAddBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setupScheduleAddForm(dialogBinding, targetDate, dialog);
        dialog.show();
    }

    /**
     * 일정 추가 다이얼로그의 권한별 유형 스피너 및 휴가 선택 시 동적 폼 변환 세팅
     */
    private void setupScheduleAddForm(DialogScheduleAddBinding dialogBinding, String targetDate, AlertDialog dialog) {
        dialogBinding.tvAddScheduleDate.setText(targetDate);

        // 권한별 일정 유형 스피너 항목 세팅 (간부: 부대/개인/휴가, 병사: 개인/휴가)
        List<String> types = new ArrayList<>();
        if ("간부".equals(memberType)) {
            types.add("부대");
        }
        types.add("개인");
        types.add("휴가");

        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, types
        );
        dialogBinding.spinnerAddType.setAdapter(spinnerAdapter);

        // 스피너 이벤트: '휴가' 선택 시 시간/장소 숨김 및 휴가 전용 폼 활성화
        dialogBinding.spinnerAddType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedType = types.get(position);
                if ("휴가".equals(selectedType)) {
                    dialogBinding.layoutTimeContainer.setVisibility(View.GONE);
                    dialogBinding.inputLocation.setVisibility(View.GONE);
                    dialogBinding.layoutVacationContainer.setVisibility(View.VISIBLE);
                    dialogBinding.inputVacationEnd.setText(targetDate);
                    dialogBinding.tvVacationDaysCalc.setText("휴가 기간: 1일");
                } else {
                    dialogBinding.layoutTimeContainer.setVisibility(View.VISIBLE);
                    dialogBinding.inputLocation.setVisibility(View.VISIBLE);
                    dialogBinding.layoutVacationContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 저장 버튼 이벤트
        dialogBinding.btnSaveSchedule.setOnClickListener(v -> {
            String title = dialogBinding.inputScheduleTitle.getText().toString().trim();
            String selectedType = (String) dialogBinding.spinnerAddType.getSelectedItem();
            String memo = dialogBinding.inputScheduleMemo.getText().toString().trim();

            if (title.isEmpty()) {
                dialogBinding.inputScheduleTitle.setError("일정 제목을 입력해 주세요.");
                return;
            }

            ScheduleItem newItem;
            if ("휴가".equals(selectedType)) {
                String endDate = dialogBinding.inputVacationEnd.getText().toString().trim();
                if (endDate.isEmpty()) endDate = targetDate;
                newItem = new ScheduleItem(targetDate, title, "00:00", "23:59", "자택", "휴가", memo);
            } else {
                String startTime = dialogBinding.inputStartTime.getText().toString().trim();
                String endTime = dialogBinding.inputEndTime.getText().toString().trim();
                String location = dialogBinding.inputLocation.getText().toString().trim();
                newItem = new ScheduleItem(targetDate, title, startTime, endTime, location, selectedType, memo);
            }

            // 간부 유저가 '부대' 일정을 등록할 경우 더미 서버(MockServerDataSource)에도 게시/저장
            if ("간부".equals(memberType) && "부대".equals(selectedType)) {
                MockServerDataSource.getInstance().publishUnitSchedule(newItem);
            }

            allScheduleList.add(0, newItem);
            updateCalendarView();
            dialog.dismiss();
        });
    }

    /**
     * 일정 상세정보 조회, 권한별 날짜/제목 수정, 메모 수정 및 삭제 2차 확인 다이얼로그
     */
    private void showScheduleDetailDialog(ScheduleItem item) {
        if (item == null) return;

        DialogScheduleDetailBinding dialogBinding = DialogScheduleDetailBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 카테고리 텍스트 및 유형별 상징 색상 바인딩
        String typeStr = (item.getType() != null) ? item.getType() : "";
        String categoryText = typeStr + " 일정";
        dialogBinding.tvDetailCategory.setText(categoryText);

        int catColorRes = "부대".equals(typeStr) ? R.color.forest_700 :
                ("휴가".equals(typeStr) ? R.color.vacation_text : R.color.personal);
        dialogBinding.tvDetailCategory.setTextColor(ContextCompat.getColor(this, catColorRes));

        dialogBinding.tvDetailTitle.setText(item.getTitle());

        String startTimeStr = (item.getStartTime() != null && !item.getStartTime().isEmpty()) ? item.getStartTime() : "-";
        String endTimeStr = (item.getEndTime() != null && !item.getEndTime().isEmpty()) ? item.getEndTime() : "-";
        String locationStr = (item.getLocation() != null && !item.getLocation().isEmpty()) ? item.getLocation() : "-";

        dialogBinding.tvDetailStart.setText("시작 시간\n" + startTimeStr);
        dialogBinding.tvDetailEnd.setText("종료 시간\n" + endTimeStr);
        dialogBinding.tvDetailLocation.setText("장소\n" + locationStr);

        final String originalTitle = (item.getTitle() != null) ? item.getTitle() : "";
        final String originalDate = (item.getDate() != null) ? item.getDate() : "";
        final String originalMemo = item.getMemo();

        final String[] currentTitleHolder = {originalTitle};
        final String[] currentDateHolder = {originalDate};

        // 개인 일정(또는 간부)인 경우 제목 터치 시 제목 수정 다이얼로그 연동
        boolean canEditTitle = "개인".equals(item.getType()) || ("간부".equals(memberType) && "부대".equals(item.getType()));
        if (canEditTitle) {
            dialogBinding.tvDetailTitle.setOnClickListener(v -> {
                android.widget.EditText inputTitle = new android.widget.EditText(this);
                inputTitle.setText(currentTitleHolder[0]);
                inputTitle.setPadding(32, 24, 32, 24);

                new AlertDialog.Builder(this)
                        .setTitle("일정 제목 수정")
                        .setView(inputTitle)
                        .setPositiveButton("확인", (d, which) -> {
                            String newTitle = inputTitle.getText().toString().trim();
                            if (!newTitle.isEmpty()) {
                                currentTitleHolder[0] = newTitle;
                                dialogBinding.tvDetailTitle.setText(newTitle);
                                checkScheduleModified(dialogBinding, currentTitleHolder[0], originalTitle, currentDateHolder[0], originalDate, originalMemo);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        }

        // 날짜 수정 권한 판별
        boolean canEditDate = "개인".equals(item.getType()) || ("간부".equals(memberType) && "부대".equals(item.getType()));

        if (!originalDate.isEmpty()) {
            dialogBinding.tvFixedMessage.setVisibility(View.VISIBLE);
            dialogBinding.tvFixedMessage.setText("일정 일자: " + currentDateHolder[0]);

            if (canEditDate) {
                dialogBinding.tvFixedMessage.setBackgroundResource(R.drawable.bg_input);
                dialogBinding.tvFixedMessage.setPadding(32, 24, 32, 24);
                dialogBinding.tvFixedMessage.setOnClickListener(v -> {
                    String[] parts = currentDateHolder[0].split("-");
                    int y = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]) - 1;
                    int d = Integer.parseInt(parts[2]);

                    DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                        currentDateHolder[0] = String.format(Locale.KOREA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        dialogBinding.tvFixedMessage.setText("일정 일자: " + currentDateHolder[0]);
                        checkScheduleModified(dialogBinding, currentTitleHolder[0], originalTitle, currentDateHolder[0], originalDate, originalMemo);
                    }, y, m, d);

                    if (enlistmentMillis > 0) picker.getDatePicker().setMinDate(enlistmentMillis);
                    if (dischargeMillis > 0) picker.getDatePicker().setMaxDate(dischargeMillis);

                    picker.show();
                });
            } else {
                // 수정 불가 항목: 상자 배경 및 클릭 제거
                dialogBinding.tvFixedMessage.setBackground(null);
                dialogBinding.tvFixedMessage.setPadding(0, 12, 0, 12);
                dialogBinding.tvFixedMessage.setOnClickListener(null);
            }
        } else {
            dialogBinding.tvFixedMessage.setVisibility(View.GONE);
        }

        // 메모 세팅
        dialogBinding.inputDetailMemo.setText(originalMemo);

        // [버튼 초기화] "수정사항 저장" 버튼 초기 비활성화
        dialogBinding.btnSaveMemo.setText("수정사항 저장");
        dialogBinding.btnSaveMemo.setEnabled(false);
        dialogBinding.btnSaveMemo.setAlpha(0.4f);

        // 메모 텍스트 변경 감지하여 저장 버튼 활성화
        dialogBinding.inputDetailMemo.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkScheduleModified(dialogBinding, currentTitleHolder[0], originalTitle, currentDateHolder[0], originalDate, originalMemo);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 닫기 버튼
        dialogBinding.btnCloseDetail.setOnClickListener(v -> dialog.dismiss());

        // 수정사항 저장 버튼 클릭 이벤트
        dialogBinding.btnSaveMemo.setOnClickListener(v -> {
            String newMemo = dialogBinding.inputDetailMemo.getText().toString().trim();
            item.setTitle(currentTitleHolder[0]);
            item.setDate(currentDateHolder[0]);
            item.setMemo(newMemo);
            Toast.makeText(this, "수정사항이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            updateCalendarView();
            dialog.dismiss();
        });

        // 삭제 권한 판별 (개인 일정 OR 간부 권한의 부대 일정)
        boolean canDelete = "개인".equals(item.getType()) || ("간부".equals(memberType) && "부대".equals(item.getType()));
        if (canDelete) {
            dialogBinding.btnDeleteSchedule.setVisibility(View.VISIBLE);
            dialogBinding.btnDeleteSchedule.setOnClickListener(v -> {
                // 삭제 2차 확인 다이얼로그
                new AlertDialog.Builder(this)
                        .setTitle("일정 삭제")
                        .setMessage("정말로 이 일정을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (confirmDialog, which) -> {
                            allScheduleList.remove(item);
                            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            updateCalendarView();
                            dialog.dismiss();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        } else {
            dialogBinding.btnDeleteSchedule.setVisibility(View.GONE);
        }

        dialog.show();
    }

    /**
     * 제목, 날짜, 메모 변경 여부 체크 후 "수정사항 저장" 버튼 활성화 제어 헬퍼 메서드
     */
    private void checkScheduleModified(DialogScheduleDetailBinding binding, String currentTitle, String originalTitle, String currentDate, String originalDate, String originalMemo) {
        String currentMemo = binding.inputDetailMemo.getText().toString().trim();
        boolean isModified = !currentTitle.equals(originalTitle) || !currentDate.equals(originalDate) || !currentMemo.equals(originalMemo);
        binding.btnSaveMemo.setEnabled(isModified);
        binding.btnSaveMemo.setAlpha(isModified ? 1.0f : 0.4f);
    }
}