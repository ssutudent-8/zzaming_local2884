package com.example.c20232884.model;

public class ScheduleItem {
    private String date;      // 날짜 (예: "2025-03-30")
    private String title;     // 일정 제목 (예: "야간 경계근무")
    private String startTime;   // 시작 시간 (예: "19:00")
    private String endTime;     // 종료 시간 (예: "20:00")
    private String location;  // 장소 (예: "초소 A")
    private String type;      // 일정 유형 (예: "개인", "부대", "휴가")
    private String memo;      // 메모/비고 (예: "방한용품 착용")

    public ScheduleItem() {}

    public ScheduleItem(String date, String title, String startTime, String endTime, String location, String type, String memo)
    {
        this.date = date;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.type = type;
        this.memo = memo;
    }

    // 리스트 표출용 시간 범위 반환 헬퍼 메서드 ("19:00 ~ 20:00")
    public String getTimeRange() {
        if (startTime != null && endTime != null && !endTime.isEmpty()) {
            return startTime + " ~ " + endTime;
        } else if (startTime != null) {
            return startTime;
        } else {
            return "";
        }
    }

    //Getter
    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getMemo() {
        return memo;
    }

    //Setter
    public void setDate(String date) {
        this.date = date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}