package me.carc.intervaltimer.utils;


import com.google.android.gms.maps.model.LatLng;

import java.text.MessageFormat;
import java.util.List;

import me.carc.intervaltimer.model.LatLon;


/**
 * Created by Carc.me on 10.10.16.
 * <p/>
 * TODO: Add a class header comment!
 */
public class MapUtils {

    private static final double BOUNDS_FROM_POINT_LIMIT = 0.00015;


    public static float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }

    public static String buildGoogleMapLink(LatLon point) {
        return "http://maps.google.com/?q=" + ((float) point.getLatitude()) + "," + ((float) point.getLongitude());
    }

    public static String buildOsmMapLink(double lat, double lon) {
        return buildOsmMapLink(new LatLon(lat, lon), 17);
    }

    public static String buildOsmMapLink(double lat, double lon, int zoom) {
        return buildOsmMapLink(new LatLon(lat, lon), zoom);
    }

    public static String buildOsmMapLink(LatLon point, int zoom) {
        return "https://www.openstreetmap.org/#map=" + zoom + "/" + ((float) point.getLatitude()) + "/" + ((float) point.getLongitude());
    }

    /**
     * Build a static map image
     * @param point GeoPoint get the lat and long values
     * @param size  String size of image (eg 300x300)
     * @param zoom  int zoom lvl
     * @return
     */
    public static String buildStaticOsmMapImage(LatLon point, String size, int zoom) {
        return buildStaticOsmMapImage(point.getLatitude(), point.getLongitude(), size, zoom);
    }

    /**
     * Build a static map image
     * @param lat   String lat
     * @param lon   String long
     * @param size  String size of image (eg 300x300)
     * @param zoom  int zoom lvl
     * @return
     */
    public static String buildStaticOsmMapImage(double lat, double lon, String size, int zoom) {
        return "http://staticmap.openstreetmap.de/staticmap.php?center=" + lat + "," + lon
                + "&zoom=" + zoom +"&size="+ size + "&maptype=mapnik&markers=" + lat + "," + lon + ",red-pushpin";
    }

    public static String buildStaticOsmMapImageMarkerRight(double lat, double lon, String size, int zoom) {
        return "http://staticmap.openstreetmap.de/staticmap.php?center=" + lat + "," + (lon - 0.002)
                + "&zoom=" + zoom +"&size="+ size + "&maptype=mapnik&markers=" + lat + "," + lon + ",red-pushpin";
    }


    public static String getFormattedDistance(double meters) {
        double mainUnitInMeters = 1000;
        String mainUnitStr = "km";
        if (meters >= 100 * mainUnitInMeters) {
            return (int) (meters / mainUnitInMeters + 0.5) + " " + mainUnitStr;
        } else if (meters > 9.99f * mainUnitInMeters) {
            return MessageFormat.format("{0,number,#.#} " + mainUnitStr, ((float) meters) / mainUnitInMeters).replace('\n', ' ');
        } else if (meters > 0.999f * mainUnitInMeters) {
            return MessageFormat.format("{0,number,#.##} " + mainUnitStr, ((float) meters) / mainUnitInMeters).replace('\n', ' ');
        } else {
            return meters + " m";
        }
    }

    public static String getFormattedAlt(double alt) {
        return ((int) (alt + 0.5)) + " m";
    }

    public static String getFormattedDuration(int seconds) {
        int hours = seconds / (60 * 60);
        int minutes = (seconds / 60) % 60;
        if (hours > 0) {
            return hours + " "
                    + "h"
                    + (minutes > 0 ? " " + minutes + " "
                    + "min" : "");
        } else {
            return minutes + " min";
        }
    }

    public static double bearingBetweenLocations(LatLon a, LatLon b) {
        if(a == null || b == null) return 0;

        double PI = 3.14159;
        double lat1 = a.getLatitude() * PI / 180;
        double long1 = a.getLongitude() * PI / 180;
        double lat2 = b.getLatitude() * PI / 180;
        double long2 = b.getLongitude() * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    public static String buildGeoUrl(double latitude, double longitude, int zoom) {
        return "geo:" + ((float) latitude) + "," + ((float) longitude) + "?z=" + zoom;
    }

    public static double getDistance(List<LatLon> list) {
        double distance = 0;
        if(list != null && list.size() >= 2) {
            for (int i = 1; i < list.size(); i++) {
                distance += getDistance(list.get(i - 1), list.get(i));
            }
        }
        return distance;
    }


    public static double getDistance(LatLng l1, LatLng l2) {
        return getDistance(l1.latitude, l1.longitude, l2.latitude, l2.longitude);
    }

    public static double getDistance(LatLon l1, LatLon l2) {
        return getDistance(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude());
    }

    public static double getDistance(LatLon p, double latitude, double longitude) {
        if(p == null) return 0;
        return getDistance(p.getLatitude(), p.getLongitude(), latitude, longitude);
    }

    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6372.8; // for haversine use R = 6372.8 km instead of 6371 km
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return (2 * R * 1000 * Math.asin(Math.sqrt(a)));
    }

    private static double toRadians(double angdeg) {
//		return Math.toRadians(angdeg);
        return angdeg / 180.0 * Math.PI;
    }


/*
    public static BoundingBox getBoundsFromPoint(LatLon p) {
        return new BoundingBox(
                p.getLatitude()  + BOUNDS_FROM_POINT_LIMIT,
                p.getLongitude() + BOUNDS_FROM_POINT_LIMIT,
                p.getLatitude()  - BOUNDS_FROM_POINT_LIMIT,
                p.getLongitude() - BOUNDS_FROM_POINT_LIMIT);
    }


    public static BoundingBox findBoundingBoxFromPointsList(List<LatLon> points, boolean addPadding) {
        double west = 0.0;
        double east = 0.0;
        double north = 0.0;
        double south = 0.0;

        for (int lc = 0; lc < points.size(); lc++) {
            LatLon point = points.get(lc);
            if (lc == 0) {
                north = point.getLatitude();
                south = point.getLatitude();
                west = point.getLongitude();
                east = point.getLongitude();
            } else {
                if (point.getLatitude() > north) {
                    north = point.getLatitude();
                } else if (point.getLatitude() < south) {
                    south = point.getLatitude();
                }
                if (point.getLongitude() < west) {
                    west = point.getLongitude();
                } else if (point.getLongitude() > east) {
                    east = point.getLongitude();
                }
            }
        }

        // OPTIONAL - Add some extra "padding" for better map display
        if(addPadding) {
            double padding = 0.01;
            north = north + padding;
            south = south - padding;
            west = west - padding;
            east = east + padding;
        }
        return new BoundingBox(north, east, south, west);
    }
*/
}

