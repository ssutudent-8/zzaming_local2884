package com.example.c20232884;

import com.example.c20232884.model.ScheduleItem;
import com.example.c20232884.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 더미 병사/간부 유저 데이터베이스 및 부대 일정 서버 DB 동기화를 시뮬레이션하는 더미 서버 클래스 (Singleton)
 */
public class MockServerDataSource {

    private static MockServerDataSource instance;
    private final List<ScheduleItem> serverUnitSchedules;

    // 더미 유저 DB (더미 병사 1명, 더미 간부 1명)
    private User dummySoldier;
    private User dummyOfficer;
    private User currentUser; // 로비 설정에서 선택되어 적용된 활성 유저 세션

    private MockServerDataSource() {
        serverUnitSchedules = new ArrayList<>();
        serverUnitSchedules.add(new ScheduleItem("2026-10-01", "국군의 날 행사", "10:00", "12:00", "연병장", "부대", "정복 착용"));
        serverUnitSchedules.add(new ScheduleItem("2026-11-15", "동계 진지공사", "08:00", "17:00", "진지 A", "부대", "방한장갑 준비"));

        // 더미 병사 & 더미 간부 객체 생성
        dummySoldier = new User("u_soldier_01", "김병장", "병사", "백골대대", "병장", "2025-06-16", "2026-12-15");
        dummyOfficer = new User("u_officer_01", "행정관", "간부", "백골대대", "상사", "2020-03-01", "2030-12-31");

        // 기본 활성 세션: 더미 병사
        currentUser = dummySoldier;
    }

    public static synchronized MockServerDataSource getInstance() {
        if (instance == null) {
            instance = new MockServerDataSource();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // 로비 설정에서 유저타입 선택 시 더미 유저 세션 자동 스위칭
    public void selectMemberType(String memberType) {
        if ("간부".equals(memberType)) {
            this.currentUser = dummyOfficer;
        } else {
            this.currentUser = dummySoldier;
        }
    }

    public User getDummySoldier() {
        return dummySoldier;
    }

    public User getDummyOfficer() {
        return dummyOfficer;
    }

    public void publishUnitSchedule(ScheduleItem schedule) {
        if (schedule != null && "부대".equals(schedule.getType())) {
            serverUnitSchedules.add(0, schedule);
        }
    }

    public List<ScheduleItem> fetchServerUnitSchedules() {
        return new ArrayList<>(serverUnitSchedules);
    }
}