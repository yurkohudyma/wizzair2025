package ua.hudyma.util;

import ua.hudyma.dto.AirportDistanceDto;

public class DistanceCalculator {
    private DistanceCalculator() {
    }

    private static final double EARTH_RADIUS_KM = 6371;

    public static double haversine(AirportDistanceDto dto) {
        var lat1 = dto.depLat();
        var lon1 = dto.depLon();
        var lat2 = dto.desLat();
        var lon2 = dto.destLon();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
