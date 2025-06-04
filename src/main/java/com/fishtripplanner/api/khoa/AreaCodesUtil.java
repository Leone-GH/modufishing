package com.fishtripplanner.api.khoa;

import java.util.Arrays;
import java.util.Optional;

public class AreaCodesUtil {

    public static Optional<AreaCodes> findNearest(double lat, double lon, AreaCodes.AreaType type) {
        return Arrays.stream(AreaCodes.values())
                .filter(ac -> ac.getType() == type)
                .min((a1, a2) -> {
                    double d1 = distance(lat, lon, Double.parseDouble(a1.getLatitude()), Double.parseDouble(a1.getLongitude()));
                    double d2 = distance(lat, lon, Double.parseDouble(a2.getLatitude()), Double.parseDouble(a2.getLongitude()));
                    return Double.compare(d1, d2);
                });
    }

    // Haversine formula
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 지구 반지름 (단위: km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
