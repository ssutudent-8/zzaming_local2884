package com.example.c20232884.model;

/**
 * 유저 프로필 및 회원 유형(memberType) 정보를 담는 User 모델 클래스
 */
public class User {
    private String userId;          // 유저 고유 ID (예: "u_soldier_01")
    private String userName;        // 이름 (예: "김병장", "행정관")
    private String memberType;      // 회원 유형 ("병사", "간부")
    private String unitId;          // 소속 부대 ("백골대대")
    private String rank;            // 계급 ("병장", "상사")
    private String enlistmentDate;  // 입대일 ("2025-06-16")
    private String dischargeDate;   // 전역일 ("2026-12-15")

    public User() {}

    public User(String userId, String userName, String memberType, String unitId, String rank, String enlistmentDate, String dischargeDate) {
        this.userId = userId;
        this.userName = userName;
        this.memberType = memberType;
        this.unitId = unitId;
        this.rank = rank;
        this.enlistmentDate = enlistmentDate;
        this.dischargeDate = dischargeDate;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    // [수정] memberType Getter & Setter
    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }

    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getEnlistmentDate() { return enlistmentDate; }
    public void setEnlistmentDate(String enlistmentDate) { this.enlistmentDate = enlistmentDate; }

    public String getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }
}