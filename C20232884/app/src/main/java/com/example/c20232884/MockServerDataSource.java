package com.example.c20232884;

import com.example.c20232884.model.ScheduleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 부대 공유 일정 및 서버 DB 동기화를 시뮬레이션하는 더미 서버 클래스 (Singleton)
 */
public class MockServerDataSource {

    private static MockServerDataSource instance;
    private final List<ScheduleItem> serverUnitSchedules;

    private MockServerDataSource() {
        serverUnitSchedules = new ArrayList<>();
        // 서버 DB 초기 백골대대 공유 부대 일정 셋업
        serverUnitSchedules.add(new ScheduleItem("2026-10-01", "국군의 날 행사", "10:00", "12:00", "연병장", "부대", "정복 착용"));
        serverUnitSchedules.add(new ScheduleItem("2026-11-15", "동계 진지공사", "08:00", "17:00", "진지 A", "부대", "방한장갑 준비"));
    }

    public static synchronized MockServerDataSource getInstance() {
        if (instance == null) {
            instance = new MockServerDataSource();
        }
        return instance;
    }

    /**
     * 간부 유저가 작성한 부대 일정을 더미 서버 DB에 게시/저장
     */
    public void publishUnitSchedule(ScheduleItem schedule) {
        if (schedule != null && "부대".equals(schedule.getType())) {
            serverUnitSchedules.add(0, schedule);
        }
    }

    /**
     * 서버 DB로부터 부대 공유 일정 수신 (병사/간부 동기화)
     */
    public List<ScheduleItem> fetchServerUnitSchedules() {
        return new ArrayList<>(serverUnitSchedules);
    }
}
