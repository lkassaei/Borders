package org.example.demo1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.shape.Polygon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class HelloApplication extends Application {

    private static Pane pane;
    private static final double WIDTH = 1500, HEIGHT = 800, MARGIN = 20;

    private static final double GLOBE_RADIUS = 200;
    private static final double DOT_RADIUS = 2;

    private static Group root;
    private static Group globeGroup;

    private static String fullJson;
    private static Country lastGuess;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);


    @Override
    public void start(Stage stage) {
        //setUpOld(stage);


        // Replace the Pane
        globeGroup = new Group();

        globeGroup.getTransforms().addAll(rotateX, rotateY);

        Sphere oceanSphere = new Sphere(GLOBE_RADIUS);
        PhongMaterial oceanMat = new PhongMaterial();
        oceanMat.setDiffuseColor(Color.web("#4a90d9"));
        oceanMat.setSpecularColor(Color.WHITE);
        oceanSphere.setMaterial(oceanMat);
        globeGroup.getChildren().add(oceanSphere);

        root = new Group(globeGroup);

        SubScene subScene = new SubScene(root, WIDTH, HEIGHT, true,
                SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#0d0d1a"));   // dark space background

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().add(new Translate(0, 0, -800));
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setFieldOfView(35);
        subScene.setCamera(camera);

        Scene scene = new Scene(new Group(subScene));
        stage.setTitle("3D World Globe");
        stage.setScene(scene);
        stage.show();

        subScene.setOnMousePressed((MouseEvent e) -> {
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });
        subScene.setOnMouseDragged((MouseEvent e) -> {
            double dx = e.getSceneX() - mouseAnchorX;
            double dy = e.getSceneY() - mouseAnchorY;
            rotateX.setAngle(anchorAngleX - dy * 0.3);
            rotateY.setAngle(anchorAngleY + dx * 0.3);
        });



        // Load GeoJSON once
        try {
            var is = HelloApplication.class.getResourceAsStream("/countries.geojson");
            fullJson = new String(is.readAllBytes());
        } catch (Exception e) {
            System.out.println("Failed to load GeoJSON: " + e.getMessage());
            return;
        }

        drawInputCountry();
    }

    public static void setUpOld(Stage stage) {
        pane = new Pane();
        pane.setStyle("-fx-background-color: #aec6cf;");

        Scene scene = new Scene(pane, WIDTH, HEIGHT);
        stage.setTitle("World Map");
        stage.setScene(scene);
        stage.show();
    }

    public static void drawALlCountries() {
        Thread inputThread = new Thread(() -> {
            try {
                InputStream is = HelloApplication.class.getResourceAsStream("/countryIn.txt");
                Scanner scanner = new Scanner(is);
                while (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) continue;
                    drawCountryByName(input);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    public static void drawInputCountry() {
        // Terminal input thread
        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter country name (or 'clear' to reset): ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("clear")) {
                    //Platform.runLater(() -> pane.getChildren().clear());
                    Platform.runLater(() -> globeGroup.getChildren().subList(1, globeGroup.getChildren().size()).clear());
                    lastGuess = null;
                } else {
                    Country country = new Country(input);
                    if (lastGuess == null) {
                        lastGuess = country;
                        drawCountryByName(input);
                    }
                    else {
                        lastGuess.loadNeighbors(lastGuess.getName());
                        System.out.println("Loaded " + lastGuess.getNeighbors().size() + " neighbors for " + lastGuess.getName());
                        for (Country n : lastGuess.getNeighbors()) {
                            System.out.println("  [" + n.getName() + "]");
                        }
                        if (lastGuess.isNeighbor(country)) {
                            lastGuess = country;
                            drawCountryByName(input);
                        }
                        else {
                            System.out.print("Not a neighbor.");
                        }
                    }
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    public static void drawCountryByName(String targetName) {
        int i = fullJson.indexOf("\"features\"");

        while (true) {
            int featureStart = fullJson.indexOf("{ \"type\": \"Feature\"", i);
            if (featureStart == -1) {
                System.out.println("Country not found: " + targetName);
                break;
            }

            int nextFeature = fullJson.indexOf("{ \"type\": \"Feature\"", featureStart + 1);
            String featureJson = nextFeature != -1
                    ? fullJson.substring(featureStart, nextFeature)
                    : fullJson.substring(featureStart);

            // Extract name
            int nameIndex = featureJson.indexOf("\"name\": \"");
            if (nameIndex == -1) { i = featureStart + 1; continue; }
            int nameStart = nameIndex + 9;
            int nameEnd = featureJson.indexOf("\"", nameStart);
            String countryName = featureJson.substring(nameStart, nameEnd);

            if (!countryName.equalsIgnoreCase(targetName)) {
                i = featureStart + 1;
                continue;
            }

            // Extract geometry type
            int geometryIndex = featureJson.indexOf("\"geometry\"");
            int typeIndex = featureJson.indexOf("\"type\"", geometryIndex);
            int typeValueStart = featureJson.indexOf("\"", typeIndex + 7) + 1;
            int typeValueEnd = featureJson.indexOf("\"", typeValueStart);
            String geomType = featureJson.substring(typeValueStart, typeValueEnd);

            // Extract coordinates JSON
            int coordsIndex = featureJson.indexOf("\"coordinates\"");
            int arrayStart = featureJson.indexOf("[", coordsIndex);
            String coordsJson = featureJson.substring(arrayStart);

            ArrayList<double[][]> polygons = new ArrayList<>();
            if (geomType.equals("Polygon")) {
                // coords = [ [ [lon,lat], ... ] ]  — extract the inner ring at depth 2
                polygons = parsePolygon(coordsJson);
            } else if (geomType.equals("MultiPolygon")) {
                polygons = parseMultiPolygon(coordsJson);
            }

            System.out.println("Drawing: " + countryName + " (" + polygons.size() + " polygon(s))");
            final ArrayList<double[][]> finalPolygons = polygons;
            //Platform.runLater(() -> drawCountry(finalPolygons, Color.LIGHTGREEN, Color.DARKGREEN));
            Platform.runLater(() -> {
                drawCountry3DFilled(finalPolygons, Color.LIGHTGREEN);
                drawCountry3D(finalPolygons, Color.DARKCYAN);
            });
            break;
        }
    }

    /**
     * Parse a Polygon coordinates block: [ [ [lon,lat], ... ], ... ]
     * Depth 1 = outer array of rings, Depth 2 = a ring, Depth 3 = a coordinate pair
     */
    public static ArrayList<double[][]> parsePolygon(String json) {
        ArrayList<double[][]> rings = new ArrayList<>();
        int depth = 0;
        int ringStart = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                depth++;
                if (depth == 2) ringStart = i; // start of a ring
            } else if (c == ']') {
                if (depth == 2 && ringStart != -1) {
                    String ringJson = json.substring(ringStart, i + 1);
                    double[][] ring = parseCoordinatePairs(ringJson);
                    if (ring.length > 0) rings.add(ring);
                    ringStart = -1;
                }
                depth--;
            }
        }
        return rings;
    }

    /**
     * Parse a MultiPolygon coordinates block: [ [ [ [lon,lat], ... ] ] ]
     * Depth 1 = outer, Depth 2 = polygon, Depth 3 = ring, Depth 4 = coord pair
     */
    public static ArrayList<double[][]> parseMultiPolygon(String json) {
        ArrayList<double[][]> polygons = new ArrayList<>();
        int depth = 0;
        int ringStart = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                depth++;
                if (depth == 3) ringStart = i; // start of a ring inside a polygon
            } else if (c == ']') {
                if (depth == 3 && ringStart != -1) {
                    String ringJson = json.substring(ringStart, i + 1);
                    double[][] ring = parseCoordinatePairs(ringJson);
                    if (ring.length > 0) polygons.add(ring);
                    ringStart = -1;
                }
                depth--;
            }
        }
        return polygons;
    }

    /**
     * Parse a flat ring: [ [lon,lat], [lon,lat], ... ]
     * Only grabs bracket pairs at depth 2 (the coordinate pairs themselves).
     */
    public static double[][] parseCoordinatePairs(String json) {
        ArrayList<double[]> points = new ArrayList<>();
        int depth = 0;
        int pairStart = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                depth++;
                if (depth == 2) pairStart = i; // [lon, lat]
            } else if (c == ']') {
                if (depth == 2 && pairStart != -1) {
                    String inner = json.substring(pairStart + 1, i).trim();
                    String[] parts = inner.split(",");
                    if (parts.length >= 2) {
                        try {
                            double lon = Double.parseDouble(parts[0].trim());
                            double lat = Double.parseDouble(parts[1].trim());
                            if (lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90) {
                                points.add(new double[]{lon, lat});
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    pairStart = -1;
                }
                depth--;
            }
        }
        return points.toArray(new double[0][]);
    }

    public static void drawCountry(ArrayList<double[][]> polygons, Color fill, Color stroke) {
        for (double[][] ring : polygons) {
            Polygon polygon = new Polygon();
            for (double[] point : ring) {
                polygon.getPoints().addAll(lonToX(point[0]), latToY(point[1]));
            }
            polygon.setFill(fill);
            polygon.setStroke(stroke);
            polygon.setStrokeWidth(0.5);
            pane.getChildren().add(polygon);
        }
    }

    public static double lonToX(double lon) {
        return MARGIN + (lon + 180) / 360.0 * (WIDTH - 2 * MARGIN);
    }

    public static double latToY(double lat) {
        return MARGIN + (1 - (lat + 90) / 180.0) * (HEIGHT - 2 * MARGIN);
    }

    public static void drawCountry3D(ArrayList<double[][]> polygons, Color color) {
        // Helps with the lighting
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(color.brighter());

        // Draw all rings in the polygon
        for (double[][] ring : polygons) {
            // Skip every 2nd point if the ring is very dense
            int step = ring.length > 500 ? 3 : ring.length > 200 ? 2 : 1;

            // Go through ring
            for (int k = 0; k < ring.length; k += step) {
                double[] point = ring[k];
                double[] xyz   = latLonToXYZ(point[1], point[0], GLOBE_RADIUS + 1.01);
                // +1 so the dots sit just above the ocean sphere surface

                // Plot the dot
                Sphere dot = new Sphere(DOT_RADIUS);
                dot.setMaterial(mat);
                dot.setTranslateX(xyz[0]);
                dot.setTranslateY(xyz[1]);
                dot.setTranslateZ(xyz[2]);
                globeGroup.getChildren().add(dot);
            }
        }
    }

    public static void drawCountry3DFilled(ArrayList<double[][]> polygons, Color color) {
        // Lighting for the points
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(color.brighter());

        // 1. Find bounding box across all rings
        double minLat = 90, maxLat = -90;
        double minLon = 180, maxLon = -180;
        // Go through rings
        for (double[][] ring : polygons) {
            // Go through points in ring and find the lowest/highest points for x and y
            for (double[] pt : ring) {
                if (pt[0] < minLon) {
                    minLon = pt[0];
                }
                if (pt[0] > maxLon) {
                    maxLon = pt[0];
                }
                if (pt[1] < minLat)  {
                    minLat  = pt[1];
                }
                if (pt[1] > maxLat)  {
                    maxLat  = pt[1];
                }
            }
        }

        // Fill it in
        double step = 0.3;

        for (double lat = minLat; lat <= maxLat; lat += step) {
            for (double lon = minLon; lon <= maxLon; lon += step) {
                // 3. Point-in-polygon test against ALL rings
                if (isPointInAnyPolygon(lat, lon, polygons)) {
                    double[] xyz = latLonToXYZ(lat, lon, GLOBE_RADIUS + 1.0);
                    Sphere dot = new Sphere(DOT_RADIUS);
                    dot.setMaterial(mat);
                    dot.setTranslateX(xyz[0]);
                    dot.setTranslateY(xyz[1]);
                    dot.setTranslateZ(xyz[2]);
                    globeGroup.getChildren().add(dot);
                }
            }
        }
    }

    // Ray casting algorithm — counts crossings of a horizontal ray
    public static boolean isPointInAnyPolygon(double lat, double lon, ArrayList<double[][]> polygons) {
        for (double[][] ring : polygons) {
            if (isPointInRing(lat, lon, ring)) return true;
        }
        return false;
    }

    public static boolean isPointInRing(double lat, double lon, double[][] ring) {
        boolean inside = false;
        int n = ring.length;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = ring[i][0], yi = ring[i][1]; // lon, lat
            double xj = ring[j][0], yj = ring[j][1];

            boolean intersects = ((yi > lat) != (yj > lat))
                    && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi);
            if (intersects) inside = !inside;
        }
        return inside;
    }

    public static double[] latLonToXYZ(double latDeg, double lonDeg, double R) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);
        double x =  -R * Math.cos(lat) * Math.sin(lon);
        double y = -R * Math.sin(lat);
        double z =  R * Math.cos(lat) * Math.cos(lon);
        return new double[]{x, y, z};
    }

    public static void main(String[] args) {
        launch();
    }
}