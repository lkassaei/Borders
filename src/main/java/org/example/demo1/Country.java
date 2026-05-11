// Imports
package org.example.demo1;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Country {
    // Name and neighbors variables
    private String name;
    private ArrayList<Country> neighbors;

    // Each ring stores its points plus precomputed bounding box
    private double[][] points;
    double minLon;
    double maxLon;
    double minLat;
    double maxLat;

    // country name (lowercase) -> list of rings hash map
    private static final Map<String, ArrayList<Country>> polygonCache = new HashMap<>();

    // Make the name and initialize neighbors
    public Country(String name) {
        this.name = name;
        this.neighbors = new ArrayList<>();
    }

    // Private constructor for building a ring with precomputed bounding box
    private Country(double[][] points) {
        this.name = null;
        this.neighbors = new ArrayList<>();
        this.points = points;
        computeBbox();
    }

    // Finds ballpark bounding box for each country for efficiencies
    private void computeBbox() {
        minLon = 180; maxLon = -180;
        minLat = 90;  maxLat = -90;
        for (double[] pt : points) {
            if (pt[0] < minLon) minLon = pt[0];
            if (pt[0] > maxLon) maxLon = pt[0];
            if (pt[1] < minLat) minLat = pt[1];
            if (pt[1] > maxLat) maxLat = pt[1];
        }
    }

    // Getter functions
    public String getName() {
        return this.name;
    }

    public double[][] getPoints() {
        return this.points;
    }

    public ArrayList<Country> getNeighbors() {
        return this.neighbors;
    }

    // Returns cached rings for a country, or null if not found
    public static ArrayList<Country> getRings(String countryName) {
        return polygonCache.get(countryName.toLowerCase());
    }

    // Call once at startup with the full GeoJSON string
    public static void parseAllCountries(String fullJson) {
        int i = fullJson.indexOf("\"features\"");
        // Go through JSON
        while (true) {
            int featureStart = fullJson.indexOf("{ \"type\": \"Feature\"", i);
            if (featureStart == -1) break;

            int nextFeature = fullJson.indexOf("{ \"type\": \"Feature\"", featureStart + 1);
            String featureJson = nextFeature != -1
                    ? fullJson.substring(featureStart, nextFeature)
                    : fullJson.substring(featureStart);

            // Get name
            int nameIndex = featureJson.indexOf("\"name\": \"");
            if (nameIndex == -1) { i = featureStart + 1; continue; }
            int nameStart  = nameIndex + 9;
            int nameEnd    = featureJson.indexOf("\"", nameStart);
            String countryName = featureJson.substring(nameStart, nameEnd);

            // Get type
            int geometryIndex  = featureJson.indexOf("\"geometry\"");
            int typeIndex      = featureJson.indexOf("\"type\"", geometryIndex);
            int typeValueStart = featureJson.indexOf("\"", typeIndex + 7) + 1;
            int typeValueEnd   = featureJson.indexOf("\"", typeValueStart);
            String geomType    = featureJson.substring(typeValueStart, typeValueEnd);

            // Get coordinates
            int coordsIndex = featureJson.indexOf("\"coordinates\"");
            int arrayStart  = featureJson.indexOf("[", coordsIndex);
            String coordsJson = featureJson.substring(arrayStart);

            // Parse based off of geometry type
            ArrayList<double[][]> rawRings = geomType.equals("Polygon")
                    ? Borders.parsePolygon(coordsJson)
                    : Borders.parseMultiPolygon(coordsJson);

            // Store data
            ArrayList<Country> rings = new ArrayList<>();
            for (double[][] raw : rawRings) rings.add(new Country(raw));

            // Put in hash map
            polygonCache.put(countryName.toLowerCase(), rings);
            i = featureStart + 1;
        }
        System.out.println("Parsed " + polygonCache.size() + " countries into cache.");
    }

    // Function later used for drawCountry3D
    public boolean isPointInRing(double lat, double lon) {
        // Free bounding box rejection using precomputed fields
        if (lon < minLon || lon > maxLon || lat < minLat || lat > maxLat) return false;

        boolean inside = false;
        int n = points.length;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points[i][0], yi = points[i][1];
            double xj = points[j][0], yj = points[j][1];
            boolean intersects = ((yi > lat) != (yj > lat))
                    && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi);
            if (intersects) inside = !inside;
        }
        return inside;
    }

    // Returns if the point is in given set of rings
    public static boolean isPointInAnyRing(double lat, double lon, ArrayList<Country> rings) {
        for (Country ring : rings) {
            if (ring.isPointInRing(lat, lon)) return true;
        }
        return false;
    }

    // Gets neighbors of country
    public ArrayList<Country> loadNeighbors(String name) {
        try {
            // Read from csv file
            InputStream is = Country.class.getClassLoader()
                    .getResourceAsStream("GEODATASOURCE-COUNTRY-BORDERS.CSV");
            String csv = new String(is.readAllBytes());
            String[] lines = csv.split("\\r?\\n");

            for (String line : lines) {
                line = line.replace("\"", "");
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                // Get name and neighbor
                String countryName  = parts[1].trim();
                String neighborName = parts[3].replace("\r", "").trim();

                // If the name matches given country then add its neighbor to neighbors
                if (countryName.equalsIgnoreCase(name)) {
                    this.neighbors.add(new Country(neighborName));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading neighbors: " + e.getMessage());
        }
        return neighbors;
    }

    // Returns if a country is a neighbor of the given country
    public boolean isNeighbor(Country country) {
        for (Country n : neighbors) {
            if (n.getName().equalsIgnoreCase(country.getName())) return true;
        }
        return false;
    }
}
