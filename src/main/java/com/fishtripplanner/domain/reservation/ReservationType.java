package com.fishtripplanner.domain.reservation;

public enum ReservationType {
    BOAT("선상낚시"),
    ROCK("갯바위"),
    ISLAND("섬"),
    FLOAT("좌대"),
    STAY("숙박/민박/캠핑");

    private final String korean;

    ReservationType(String korean) {
        this.korean = korean;
    }

    public String getKorean() {
        return korean;
    }
}