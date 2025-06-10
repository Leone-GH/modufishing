package com.fishtripplanner.dto;

public enum FishingApiType {
    ROCK, BOAT, ETC, TRIP;

    public static FishingApiType from(String type) {
        try {
            return FishingApiType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("지원하지 않는 API 타입입니다: " + type);
        }
    }
}
