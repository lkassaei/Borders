package org.example.demo1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.TextField;
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
    private static int numMistakes;
    private static int validGuesses;
    private static ArrayList<Country> possibleNeighbors;
    private static javafx.scene.control.Button activeButton = null;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    private static final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private static final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate pivot = new Translate(0, 0, 0);

    private static final String[][] CONTINENT_COUNTRIES = {
            {"North America", "80", "44", "United States of America", "Canada", "Mexico", "Guatemala", "Belize", "Honduras", "El Salvador", "Nicaragua", "Costa Rica", "Panama", "Cuba", "Jamaica", "Haiti", "Dominican Republic", "Puerto Rico", "Trinidad and Tobago", "Bahamas", "Barbados", "Saint Lucia", "Grenada", "Saint Vincent and the Grenadines", "Antigua and Barbuda", "Dominica", "Saint Kitts and Nevis"},
            {"South America", "122", "-21", "Brazil", "Argentina", "Chile", "Colombia", "Venezuela", "Peru", "Ecuador", "Bolivia", "Paraguay", "Uruguay", "Guyana", "Suriname", "French Guiana"},
            {"Europe", "197", "38", "France", "Germany", "United Kingdom", "Italy", "Spain", "Poland", "Romania", "Netherlands", "Belgium", "Sweden", "Norway", "Denmark", "Finland", "Switzerland", "Austria", "Portugal", "Czech Republic", "Hungary", "Slovakia", "Bulgaria", "Serbia", "Croatia", "Bosnia and Herzegovina", "Slovenia", "Montenegro", "Albania", "North Macedonia", "Greece", "Cyprus", "Malta", "Luxembourg", "Iceland", "Ireland", "Estonia", "Latvia", "Lithuania", "Belarus", "Ukraine", "Moldova"},
            {"Africa", "200", "-1", "Nigeria", "Ethiopia", "Egypt", "Democratic Republic of the Congo", "Tanzania", "Kenya", "South Africa", "Uganda", "Algeria", "Sudan", "Morocco", "Angola", "Mozambique", "Ghana", "Madagascar", "Cameroon", "Ivory Coast", "Niger", "Mali", "Burkina Faso", "Malawi", "Zambia", "Senegal", "Chad", "Somalia", "Zimbabwe", "Guinea", "Rwanda", "Benin", "Burundi", "Tunisia", "South Sudan", "Togo", "Sierra Leone", "Libya", "Congo", "Liberia", "Central African Republic", "Mauritania", "Eritrea", "Namibia", "Gambia", "Botswana", "Gabon", "Lesotho", "Guinea-Bissau", "Equatorial Guinea", "Mauritius", "Eswatini", "Djibouti", "Comoros", "Cape Verde", "Sao Tome and Principe", "Seychelles"},
            {"Asia", "257", "32", "China", "India", "Indonesia", "Pakistan", "Bangladesh", "Japan", "Philippines", "Vietnam", "Turkey", "Iran", "Thailand", "Myanmar", "South Korea", "Iraq", "Afghanistan", "Saudi Arabia", "Uzbekistan", "Malaysia", "Yemen", "Nepal", "North Korea", "Sri Lanka", "Kazakhstan", "Syria", "Cambodia", "Jordan", "Azerbaijan", "United Arab Emirates", "Tajikistan", "Israel", "Laos", "Lebanon", "Kyrgyzstan", "Turkmenistan", "Singapore", "Oman", "Kuwait", "Georgia", "Mongolia", "Armenia", "Qatar", "Bahrain", "Timor-Leste", "Cyprus", "Bhutan", "Maldives", "Brunei", "Russia"},
            {"Oceania", "-12", "-26", "Australia", "Papua New Guinea", "New Zealand", "Fiji", "Solomon Islands", "Vanuatu", "Samoa", "Kiribati", "Tonga", "Micronesia", "Palau", "Marshall Islands", "Nauru", "Tuvalu"},
            {"Antarctica", "195", "-95", "Antarctica"}
    };


    @Override
    public void start(Stage stage) {
        rotateY.setAngle(0);
        rotateX.setAngle(0);
        //setUpOld(stage);
        possibleNeighbors = new ArrayList<>();

        // Replace the Pane
        globeGroup = new Group();

        globeGroup.getTransforms().addAll(pivot, rotateX, rotateY);

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

        Pane rootPane = new Pane();
        rootPane.getChildren().add(subScene);

        Scene scene = new Scene(rootPane, WIDTH, HEIGHT);
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
            rotateX.setAngle(anchorAngleX - dy * 0.3);  // - changed to +
            rotateY.setAngle(anchorAngleY + dx * 0.3);  // + changed to -
        });
        subScene.setOnMouseDragged((MouseEvent e) -> {
            double dx = e.getSceneX() - mouseAnchorX;
            double dy = e.getSceneY() - mouseAnchorY;
            rotateX.setAngle(anchorAngleX + dy * 0.3);
            rotateY.setAngle(anchorAngleY - dx * 0.3);
            System.out.println("rotateY=" + rotateY.getAngle() + ", rotateX=" + rotateX.getAngle()); // ADD THIS
        });

        try {
            InputStream is = HelloApplication.class.getResourceAsStream("/countries.geojson");
            String fullJson = new String(is.readAllBytes());
            Country.parseAllCountries(fullJson);  // fullJson field no longer needed
        } catch (Exception e) {
            System.out.println("Failed to load GeoJSON: " + e.getMessage());
            return;
        }

        //drawInputCountryTerminal();
        drawInputCountryTextBox(rootPane);
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

    public static void drawInputCountryTextBox(Pane rootPane) {
        TextField textField = new TextField();
        textField.setPromptText("Enter a country...");

        // Position it (top-left for now)
        textField.setLayoutX(80);
        textField.setLayoutY(80);
        textField.setPrefWidth(250);

        // Handle ENTER key
        textField.setOnAction(e -> {
            String input = textField.getText().trim();

            if (input.isEmpty()) return;

            if (input.equalsIgnoreCase("clear")) {
                globeGroup.getChildren().subList(1, globeGroup.getChildren().size()).clear();
                lastGuess = null;
                possibleNeighbors.clear();
                numMistakes = 0;

            } else {
                Country country = new Country(input);
                ArrayList<Country> newNeighbors = new ArrayList<>();
                for (Country neighbor : country.loadNeighbors(input)) {
                    newNeighbors.add(neighbor);
                }

                System.out.println("Input: [" + input + "]");
                System.out.println("Neighbors loaded: " + newNeighbors.size());
                System.out.println("Possible neighbors pool size: " + possibleNeighbors.size());
                for (Country c : possibleNeighbors) {
                    System.out.println("  pool: [" + c.getName() + "]");
                }

                if (!isCountry(input)) {
                    System.out.println("Not a country. Try again.");
                }
                else if (lastGuess == null) {
                    lastGuess = country;
                    for (Country c : newNeighbors) {
                        possibleNeighbors.add(c);
                    }
                    drawCountryByName(input);
                    String[] continent = findContinent(input);
                    if (continent != null) {
                        double spin = Double.parseDouble(continent[1]);
                        double tilt = Double.parseDouble(continent[2]);
                        centerGlobeOn(spin, tilt);
                    }
                } else {
                    boolean valid = false;
                    for (Country c : possibleNeighbors) {
                        if (c.getName().equalsIgnoreCase(country.getName())) {
                            valid = true;
                            validGuesses++;
                            lastGuess = country;
                            drawCountryByName(input);
                            String[] continent = findContinent(input);
                            if (continent != null) {
                                double spin = Double.parseDouble(continent[1]);
                                double tilt = Double.parseDouble(continent[2]);
                                centerGlobeOn(spin, tilt);
                            }
                            if (validGuesses >= 193) {
                                showGameOver(rootPane);
                            }
                            break;
                        }
                    }
                    if (valid) {
                        for (Country newC : newNeighbors) {
                            possibleNeighbors.add(newC);
                        }
                    }
                    if (!valid) {
                        System.out.println("Not a neighbor.");
                        numMistakes++;
                        if (numMistakes >= 10) {
                            showGameOver(rootPane);
                        }
                        else {
                            drawMistakeMarker(rootPane);
                        }
                    }
                }
            }

            textField.clear(); // optional: clears after enter
        });

        rootPane.getChildren().add(textField);
        //createContinentButtons(rootPane);
    }

    public static boolean isCountry(String countryName) {
        for (String[] continent : CONTINENT_COUNTRIES) {
            for (int i = 3; i < continent.length; i++) {  // countries start at index 3
                if (continent[i].equalsIgnoreCase(countryName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void drawInputCountryTerminal() {
        // Terminal input thread
        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter country name (or 'clear' to reset): ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("clear") || numMistakes >= 10) {
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

    public static String[] findContinent(String countryName) {
        for (String[] continent : CONTINENT_COUNTRIES) {
            for (int i = 3; i < continent.length; i++) {  // countries start at index 3
                if (continent[i].equalsIgnoreCase(countryName)) {
                    return continent;  // returns {name, rotateY, rotateX, ...countries}
                }
            }
        }
        return null;
    }

    public static void drawMistakeMarker(Pane rootPane) {
        double cx = 50 + (numMistakes - 1) * 40;  // center X of the X shape
        double cy = 50;

        double size = 12;

        javafx.scene.shape.Line line1 = new javafx.scene.shape.Line(
                cx - size, cy - size, cx + size, cy + size
        );
        javafx.scene.shape.Line line2 = new javafx.scene.shape.Line(
                cx + size, cy - size, cx - size, cy + size
        );

        line1.setStroke(Color.RED);
        line2.setStroke(Color.RED);
        line1.setStrokeWidth(3);
        line2.setStrokeWidth(3);

        rootPane.getChildren().addAll(line1, line2);
    }

    public static void showGameOver(Pane rootPane) {
        // Clear the 3D globe countries
        globeGroup.getChildren().subList(1, globeGroup.getChildren().size()).clear();

        // Remove everything from rootPane except the SubScene (index 0)
        rootPane.getChildren().subList(1, rootPane.getChildren().size()).clear();

        javafx.scene.text.Text text;
        if (validGuesses < 193) {
            // Big "YOU LOSE" text
            text = new javafx.scene.text.Text("YOU LOSE");
            text.setFill(Color.RED);
        }
        else {
            // Big "YOU LOSE" text
            text = new javafx.scene.text.Text("YOU WIN!");
            text.setFill(Color.GREEN);
        }

        text.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 120));

        // Center it on screen
        text.setLayoutX(WIDTH / 2 - 310);
        text.setLayoutY(HEIGHT / 2 + 40);

        rootPane.getChildren().add(text);
    }

    public static void drawCountryByName(String targetName) {
        ArrayList<Country> rings = Country.getRings(targetName);
        if (rings == null) {
            System.out.println("Country not found: " + targetName);
            return;
        }
        System.out.println("Drawing: " + targetName + " (" + rings.size() + " ring(s))");
        Platform.runLater(() -> {
            drawCountry3DFilled(rings, Color.LIGHTGREEN);
            drawCountry3D(rings, Color.DARKCYAN);
        });
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


    public static void drawCountry3D(ArrayList<Country> rings, Color color) {
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(color.brighter());

        for (Country ring : rings) {
            double[][] pts = ring.getPoints();
            int step = pts.length > 500 ? 3 : pts.length > 200 ? 2 : 1;
            for (int k = 0; k < pts.length; k += step) {
                double[] xyz = latLonToXYZ(pts[k][1], pts[k][0], GLOBE_RADIUS + 1.01);
                Sphere dot = new Sphere(DOT_RADIUS);
                dot.setMaterial(mat);
                dot.setTranslateX(xyz[0]);
                dot.setTranslateY(xyz[1]);
                dot.setTranslateZ(xyz[2]);
                globeGroup.getChildren().add(dot);
            }
        }
    }

    public static void drawCountry3DFilled(ArrayList<Country> rings, Color color) {
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(color.brighter());

        // Bounding box across all rings — O(n_rings) using stored fields
        double minLat = rings.stream().mapToDouble(r -> r.minLat).min().orElse(-90);
        double maxLat = rings.stream().mapToDouble(r -> r.maxLat).max().orElse(90);
        double minLon = rings.stream().mapToDouble(r -> r.minLon).min().orElse(-180);
        double maxLon = rings.stream().mapToDouble(r -> r.maxLon).max().orElse(180);

        double step = 0.7;
        for (double lat = minLat; lat <= maxLat; lat += step) {
            for (double lon = minLon; lon <= maxLon; lon += step) {
                if (Country.isPointInAnyRing(lat, lon, rings)) {
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

    public static double[] latLonToXYZ(double latDeg, double lonDeg, double R) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);
        double x =  -R * Math.cos(lat) * Math.sin(lon);
        double y = -R * Math.sin(lat);
        double z =  R * Math.cos(lat) * Math.cos(lon);
        return new double[]{x, y, z};
    }

    public static void centerGlobeOn(double spinLeftRight, double tiltUpDown) {
        Platform.runLater(() -> {
            rotateY.setAngle(spinLeftRight);
            rotateX.setAngle(tiltUpDown);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}