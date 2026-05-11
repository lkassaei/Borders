// Original Game Borders By Lily Kassaei

// Imports
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Borders extends Application {

    // Front-end variables
    private static Pane pane;
    private static final double WIDTH = 1500, HEIGHT = 800, MARGIN = 20;

    // Radius constants
    private static final double GLOBE_RADIUS = 200;
    private static final double DOT_RADIUS = 2;

    // Globe variables
    private static Group root;
    private static Group globeGroup;

    private static final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private static final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate pivot = new Translate(0, 0, 0);

    // Logic variables
    private static Country lastGuess;
    private static int numMistakes;
    private static int validGuesses;
    private static ArrayList<Country> possibleNeighbors;

    // Mouse variables
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    // Array for all countries in each continent, and the rotation values needed to see that continent
    private static final String[][] CONTINENT_COUNTRIES = {
            {"North America", "80", "44", "United States of America", "Canada", "Mexico", "Guatemala", "Belize", "Honduras", "El Salvador", "Nicaragua", "Costa Rica", "Panama", "Cuba", "Jamaica", "Haiti", "Dominican Republic", "Puerto Rico", "Trinidad and Tobago", "Bahamas", "Barbados", "Saint Lucia", "Grenada", "Saint Vincent and the Grenadines", "Antigua and Barbuda", "Dominica", "Saint Kitts and Nevis"},
            {"South America", "122", "-21", "Brazil", "Argentina", "Chile", "Colombia", "Venezuela", "Peru", "Ecuador", "Bolivia", "Paraguay", "Uruguay", "Guyana", "Suriname", "French Guiana"},
            {"Europe", "197", "38", "Cyprus", "Greenland", "France", "Germany", "United Kingdom", "Italy", "Spain", "Poland", "Romania", "Netherlands", "Belgium", "Sweden", "Norway", "Denmark", "Finland", "Switzerland", "Austria", "Portugal", "Czech Republic", "Hungary", "Slovakia", "Bulgaria", "Serbia", "Croatia", "Bosnia and Herzegovina", "Slovenia", "Montenegro", "Albania", "North Macedonia", "Greece", "Cyprus", "Malta", "Luxembourg", "Iceland", "Ireland", "Estonia", "Latvia", "Lithuania", "Belarus", "Ukraine", "Moldova"},
            {"Africa", "200", "-1", "Nigeria", "Ethiopia", "Egypt", "Democratic Republic of the Congo", "Tanzania", "Kenya", "South Africa", "Uganda", "Algeria", "Sudan", "Morocco", "Angola", "Mozambique", "Ghana", "Madagascar", "Cameroon", "Ivory Coast", "Niger", "Mali", "Burkina Faso", "Malawi", "Zambia", "Senegal", "Chad", "Somalia", "Zimbabwe", "Guinea", "Rwanda", "Benin", "Burundi", "Tunisia", "South Sudan", "Togo", "Sierra Leone", "Libya", "Congo", "Liberia", "Central African Republic", "Mauritania", "Eritrea", "Namibia", "Gambia", "Botswana", "Gabon", "Lesotho", "Guinea-Bissau", "Equatorial Guinea", "Mauritius", "Eswatini", "Djibouti", "Comoros", "Cape Verde", "Sao Tome and Principe", "Seychelles"},
            {"Asia", "257", "32", "Palestine", "Taiwan", "China", "India", "Indonesia", "Pakistan", "Bangladesh", "Japan", "Philippines", "Vietnam", "Turkey", "Iran", "Thailand", "Myanmar", "South Korea", "Iraq", "Afghanistan", "Saudi Arabia", "Uzbekistan", "Malaysia", "Yemen", "Nepal", "North Korea", "Sri Lanka", "Kazakhstan", "Syria", "Cambodia", "Jordan", "Azerbaijan", "United Arab Emirates", "Tajikistan", "Israel", "Laos", "Lebanon", "Kyrgyzstan", "Turkmenistan", "Singapore", "Oman", "Kuwait", "Georgia", "Mongolia", "Armenia", "Qatar", "Bahrain", "Timor-Leste", "Cyprus", "Bhutan", "Maldives", "Brunei", "Russia"},
            {"Oceania", "-12", "-26", "Australia", "Papua New Guinea", "New Zealand", "Fiji", "Solomon Islands", "Vanuatu", "Samoa", "Kiribati", "Tonga", "Micronesia", "Palau", "Marshall Islands", "Nauru", "Tuvalu"},
            {"Antarctica", "195", "-95", "Antarctica"}
    };

    private static int threshold = 2;
    private static String[] words;
    private static String pendingSuggestion = null;


    private static javafx.animation.Timeline gameTimer;
    private static int secondsElapsed = 0;


    @Override
    public void start(Stage stage) {
        // Reset the rotation of the globe
        rotateY.setAngle(0);
        rotateX.setAngle(0);

        // Declare array for neighbors
        possibleNeighbors = new ArrayList<>();

        // Declare globe group and add the transforms allowing it to turn
        globeGroup = new Group();
        globeGroup.getTransforms().addAll(pivot, rotateX, rotateY);

        // Draw the globe and add lighting
        Sphere oceanSphere = new Sphere(GLOBE_RADIUS);
        PhongMaterial oceanMat = new PhongMaterial();
        oceanMat.setDiffuseColor(Color.web("#4a90d9"));
        oceanMat.setSpecularColor(Color.WHITE);
        oceanSphere.setMaterial(oceanMat);
        globeGroup.getChildren().add(oceanSphere);

        // Make dark space background
        root = new Group(globeGroup);
        SubScene subScene = new SubScene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#0d0d1a"));

        // Add the camera for 3D viewing
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().add(new Translate(0, 0, -800));
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setFieldOfView(35);
        subScene.setCamera(camera);

        // Create pane so we can add things like textBoxes, etc.
        Pane rootPane = new Pane();
        rootPane.getChildren().add(subScene);

        // Create the window
        Scene scene = new Scene(rootPane, WIDTH, HEIGHT);
        stage.setTitle("3D World Globe");
        stage.setScene(scene);
        stage.show();

        // Set up mouse events so we can move it and move the globe
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
        // Debug statements so we can see the rotation values
        subScene.setOnMouseDragged((MouseEvent e) -> {
            double dx = e.getSceneX() - mouseAnchorX;
            double dy = e.getSceneY() - mouseAnchorY;
            rotateX.setAngle(anchorAngleX + dy * 0.3);
            rotateY.setAngle(anchorAngleY - dx * 0.3);
            System.out.println("rotateY=" + rotateY.getAngle() + ", rotateX=" + rotateX.getAngle()); // ADD THIS
        });

        // Load the GeoJSON only once and store
        try {
            InputStream is = Borders.class.getResourceAsStream("/countries.geojson");
            String fullJson = new String(is.readAllBytes());
            Country.parseAllCountries(fullJson);  // fullJson field no longer needed
        } catch (Exception e) {
            System.out.println("Failed to load GeoJSON: " + e.getMessage());
            return;
        }

        // Get input from text box
        drawInputCountryTextBox(rootPane);
    }

    // Old function if we still want the 2D map sometime
    public static void setUpOld(Stage stage) {
        pane = new Pane();
        pane.setStyle("-fx-background-color: #aec6cf;");

        Scene scene = new Scene(pane, WIDTH, HEIGHT);
        stage.setTitle("World Map");
        stage.setScene(scene);
        stage.show();
    }

    // Old function if we want all countries to pop up without input
    public static void drawALlCountries() {
        Thread inputThread = new Thread(() -> {
            try {
                InputStream is = Borders.class.getResourceAsStream("/countryIn.txt");
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

    // Function to get user input from the text box and draw the country
    public static void drawInputCountryTextBox(Pane rootPane) {
        // Set up text box
        TextField textField = new TextField();
        textField.setPromptText("Enter a country...");

        // Position it
        textField.setLayoutX(80);
        textField.setLayoutY(80);
        textField.setPrefWidth(250);

        javafx.scene.text.Text suggestionLabel = new javafx.scene.text.Text("");
        suggestionLabel.setFill(Color.YELLOW);
        suggestionLabel.setFont(javafx.scene.text.Font.font("Arial", 16));
        suggestionLabel.setLayoutX(80);   // aligns under the text field
        suggestionLabel.setLayoutY(120);

        rootPane.getChildren().add(suggestionLabel);

        // Timer label
        javafx.scene.text.Text timerLabel = new javafx.scene.text.Text("Time: 0s");
        timerLabel.setFill(Color.WHITE);
        timerLabel.setFont(javafx.scene.text.Font.font("Arial", 16));
        timerLabel.setLayoutX(80);
        timerLabel.setLayoutY(145);
        rootPane.getChildren().add(timerLabel);

        javafx.scene.text.Text countLabel = new javafx.scene.text.Text(validGuesses + "/196");
        countLabel.setFill(Color.WHITE);
        countLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 20));
        countLabel.setLayoutX(80);
        countLabel.setLayoutY(165);
        rootPane.getChildren().add(countLabel);

        // Start the timer
        secondsElapsed = 0;
        gameTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    secondsElapsed++;
                    int mins = secondsElapsed / 60;
                    int secs = secondsElapsed % 60;
                    timerLabel.setText(mins > 0 ? "Time: " + mins + "m " + secs + "s" : "Time: " + secs + "s");
                })
        );
        gameTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        gameTimer.play();

        // Handle ENTER key
        textField.setOnAction(e -> {
            String input = textField.getText().trim();

            if (input.isEmpty()) {
                return;
            }

            // Clear everything if user says
            if (input.equalsIgnoreCase("clear")) {
                globeGroup.getChildren().subList(1, globeGroup.getChildren().size()).clear();
                lastGuess = null;
                possibleNeighbors.clear();
                numMistakes = 0;
                secondsElapsed = 0;
                timerLabel.setText("Time: 0s");

            }
            // Handle if country is misspelled but recognized
            else if (input.equalsIgnoreCase("yes") && pendingSuggestion != null) {
                String confirmed = pendingSuggestion;
                pendingSuggestion = null;
                suggestionLabel.setText("");

                // Make new country for the input string and find neighbors
                Country country = new Country(confirmed);
                ArrayList<Country> newNeighbors = new ArrayList<>(country.loadNeighbors(confirmed));

                // Handle if first guess
                if (lastGuess == null) {
                    // Update last guess and valid guesses
                    lastGuess = country;
                    validGuesses++;
                    countLabel.setText(validGuesses + "/196");
                    // Add neighbors to possible
                    for (Country c : newNeighbors) possibleNeighbors.add(c);
                    // Draw and center by continent
                    drawCountryByName(confirmed);
                    String[] continent = findContinent(confirmed);
                    if (continent != null) centerGlobeOn(
                            Double.parseDouble(continent[1]),
                            Double.parseDouble(continent[2])
                    );
                }
                // Handle if not first guess
                else {
                    boolean valid = false;
                    // Go through possible neighbors
                    for (Country c : possibleNeighbors) {
                        // If valid
                        if (c.getName().equalsIgnoreCase(confirmed)) {
                            valid = true;
                            // Update guesses and last guess
                            validGuesses++;
                            countLabel.setText(validGuesses + "/196");
                            lastGuess = country;
                            // Draw and center by continent
                            drawCountryByName(confirmed);
                            String[] continent = findContinent(confirmed);
                            if (continent != null) centerGlobeOn(
                                    Double.parseDouble(continent[1]),
                                    Double.parseDouble(continent[2])
                            );
                            // Check if game is over
                            if (validGuesses >= 193) showGameOver(rootPane);
                            break;
                        }
                    }
                    // Handle if valid guess
                    if (valid) {
                        // Add neighbors
                        for (Country newC : newNeighbors) possibleNeighbors.add(newC);
                    } else {
                        // Increment mistakes and check if game is lost
                        System.out.println("Not a neighbor.");
                        numMistakes++;
                        if (numMistakes >= 10) showGameOver(rootPane);
                        else drawMistakeMarker(rootPane);
                    }
                }
            }
            // If not misspelled
            else {
                // Make new country for the input string and find neighbors
                Country country = new Country(input);
                ArrayList<Country> newNeighbors = new ArrayList<>();
                for (Country neighbor : country.loadNeighbors(input)) {
                    newNeighbors.add(neighbor);
                }

                // Print neighbors for debugging
                System.out.println("Input: [" + input + "]");
                System.out.println("Neighbors loaded: " + newNeighbors.size());
                System.out.println("Possible neighbors pool size: " + possibleNeighbors.size());
                for (Country c : possibleNeighbors) {
                    System.out.println("  pool: [" + c.getName() + "]");
                }

                // If we cannot guess the input because it's not in the dataset
                if (!isCountry(input)) {
                    makeWordsArray();
                    String suggestion = suggest(input);
                    if (suggestion != null) {
                        pendingSuggestion = suggestion;
                        String display = suggestion.substring(0, 1).toUpperCase() + suggestion.substring(1);
                        suggestionLabel.setText("Did you mean: " + display + "? (type 'yes' to confirm)");
                    } else {
                        pendingSuggestion = null;
                        suggestionLabel.setText("\"" + input + "\" not recognized.");
                    }
                }
                // If its valid and we have no prior guesses
                else if (lastGuess == null) {
                    suggestionLabel.setText("");
                    pendingSuggestion = null;
                    lastGuess = country;
                    validGuesses++;
                    countLabel.setText(validGuesses + "/196");
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
                }
                // If its valid and we have a previous guess
                else {
                    boolean valid = false;
                    // Go through possible neighbors
                    for (Country c : possibleNeighbors) {
                        if (c.getName().equalsIgnoreCase(country.getName())) {
                            valid = true;
                            validGuesses++;
                            countLabel.setText(validGuesses + "/196");
                            lastGuess = country;
                            suggestionLabel.setText("");
                            pendingSuggestion = null;

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
                    // If valid guess then add all new possible neighbors
                    if (valid) {
                        for (Country newC : newNeighbors) {
                            possibleNeighbors.add(newC);
                        }
                    }
                    // If not valid then prompt again and tally mistakes
                    if (!valid) {
                        System.out.println("Not a neighbor.");
                        numMistakes++;
                        if (numMistakes >= 10) {
                            showGameOver(rootPane);
                        } else {
                            drawMistakeMarker(rootPane);
                        }
                    }
                }
            }
            // Clears after enter key
            textField.clear();
        });

        // Draw text box
        rootPane.getChildren().add(textField);
    }

    // Validate if a given country is in our dataset
    public static boolean isCountry(String countryName) {
        // Go through dataset
        for (String[] continent : CONTINENT_COUNTRIES) {
            // Countries start at index three because first 3 are the continent, x rotation value, and y rotation value
            for (int i = 3; i < continent.length; i++) {
                // If we have a match then validate
                if (continent[i].equalsIgnoreCase(countryName)) {
                    return true;
                }
            }
        }
        // No match so not a country
        return false;
    }

    public static void makeWordsArray() {
        // Build the flat words array from CONTINENT_COUNTRIES
        ArrayList<String> wordList = new ArrayList<>();
        for (String[] continent : CONTINENT_COUNTRIES) {
            for (int i = 3; i < continent.length; i++) {
                wordList.add(continent[i].toLowerCase());
            }
        }
        words = wordList.toArray(new String[0]);
    }

    public static String suggest(String typed) {
        String lower = typed.toLowerCase();
        int[][] pairs = new int[676][];
        boolean[] isCandidate = new boolean[words.length];

        // Build bigram index
        ArrayList<Integer>[] bigramLists = new ArrayList[676];
        for (int i = 0; i < 676; i++) bigramLists[i] = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            for (int j = 0; j < w.length() - 1; j++) {
                char c1 = w.charAt(j), c2 = w.charAt(j + 1);
                if (c1 >= 'a' && c1 <= 'z' && c2 >= 'a' && c2 <= 'z') {
                    bigramLists[(c1 - 'a') * 26 + (c2 - 'a')].add(i);
                }
            }
        }
        for (int i = 0; i < 676; i++) {
            pairs[i] = bigramLists[i].stream().mapToInt(x -> x).toArray();
        }

        findBigramCandidates(lower, pairs, isCandidate);
        findOtherCandidates(lower, isCandidate);

        ArrayList<Integer> candidateIndices = new ArrayList<>();
        for (int i = 0; i < isCandidate.length; i++) {
            if (isCandidate[i]) candidateIndices.add(i);
        }

        int[] ed = new int[words.length];
        performLevenshtein(lower, candidateIndices, ed);

        ArrayList<String> results = sort(candidateIndices, ed);
        return results.isEmpty() ? null : results.get(0);
    }

    // Find all the words that are candidates based on bigrams in the typed word
    public static void findBigramCandidates(String typed, int[][] pairs, boolean[] isCandidate) {
        for (int i = 0; i < typed.length() - 1; i++) {
            // Char one of the pair/bigram
            char c1 = typed.charAt(i);
            // Char two of the pair/bigram
            char c2 = typed.charAt(i + 1);
            if (c1 >= 'a' && c1 <= 'z' && c2 >= 'a' && c2 <= 'z') {
                // Bring the bigram back to an index 0-675 so we can map to the right row in pairs
                // Ex. AA = 0
                int index = (c1 - 'a') * 26 + (c2 - 'a');
                // Go through the row in pairs at index and mark the index of the work in the dict as a candidate
                for (int idx : pairs[index]) {
                    isCandidate[idx] = true;
                }
            }
        }
    }

    // Bigram doesn't work for smaller words so also get words that could work based on length
    public static void findOtherCandidates(String typed, boolean[] isCandidate) {
        for (int i = 0; i < words.length; i++) {
            if (Math.abs(words[i].length() - typed.length()) <= threshold) {
                isCandidate[i] = true;
            }
        }
    }

    // Levenshtein on all of the candidates with the results stored in ed
    public static void performLevenshtein(String typed, ArrayList<Integer> candidateIndices, int[] ed) {
        // Go through all the candidates
        for (int i : candidateIndices) {
            // Array for tabulation
            int[][] tab = new int[typed.length() + 1][words[i].length() + 1];
            // Boolean so that if a word passes threshold we can immediately move on to the next
            boolean isInvalid = false;

            // Compute Levenshtein distance
            for (int j = 0; j < tab.length; j++) {
                for (int k = 0; k < tab[0].length; k++) {
                    // If one word is shorter than the other than distance is just length of other word
                    if (j == 0 && k != 0) {
                        tab[j][k] = k;
                    }
                    else if (k == 0 && j != 0) {
                        tab[j][k] = j;
                    }
                    // The spot at (0,0)
                    else if (j == 0) {
                        tab[j][k] = 0;
                    }
                    // If the chars match, just take the old value of the distance
                    else if (typed.charAt(j - 1) == words[i].charAt(k - 1)) {
                        tab[j][k] = tab[j - 1][k - 1];
                    }
                    // Take the minimum of substitution, deletion and insertion
                    else {
                        int first = Math.min(tab[j - 1][k - 1], tab[j - 1][k]);
                        tab[j][k] = 1 + Math.min(first, tab[j][k - 1]);
                    }
                }

                // After filling each row, find the minimum value in it to see if we can break out
                int rowMin = Integer.MAX_VALUE;
                for (int k = 0; k < tab[0].length; k++) {
                    rowMin = Math.min(rowMin, tab[j][k]);
                }

                // If even the best cell in this row exceeds threshold the word will definitely exceed
                if (rowMin > threshold) {
                    isInvalid = true;
                    break;
                }
            }

            // If we broke out, then the last square is null so we just set to threshold + 1 to eliminate
            if (isInvalid) {
                ed[i] = threshold + 1;
            }
            // Else just set the last square to be the edit distance
            else {
                ed[i] = tab[tab.length - 1][tab[0].length - 1];
            }
        }
    }

    // Sort the results of Levenshtein based alphabetical value and edit distance
    public static ArrayList<String> sort(ArrayList<Integer> candidateIndices, int[] ed) {
        // Get original indices
        Integer[] indices = new Integer[words.length];
        for (int i = 0; i < words.length; i++) {
            indices[i] = i;
        }

        // Sorts by edit distance
        // Arrays.sort with a comparator:
        // Compares two indices a and b:
        // If their edit distances differ, the one with the smaller distance goes first
        // If their edit distances are equal, sort alphabetically by word
        // Sort just the candidates by edit distance, then alphabetically
        candidateIndices.sort((a, b) -> {
            if (ed[a] != ed[b]) return ed[a] - ed[b];
            return words[a].compareTo(words[b]);
        });

        // Only keep those at or below threshold
        ArrayList<String> ans = new ArrayList<>();
        for (int i : candidateIndices) {
            if (ed[i] <= threshold) {
                ans.add(words[i]);
            }
        }
        return ans;
    }

    // Old function if we still want terminal input someday
    public static void drawInputCountryTerminal() {
        // Terminal input thread
        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Prompt and scanner setup
                System.out.print("Enter country name (or 'clear' to reset): ");
                String input = scanner.nextLine().trim();

                // Clear screen if user lost or we want it to clear
                if (input.equalsIgnoreCase("clear") || numMistakes >= 10) {
                    Platform.runLater(() -> globeGroup.getChildren().subList(1, globeGroup.getChildren().size()).clear());
                    lastGuess = null;
                }
                else {
                    // Make country from input
                    Country country = new Country(input);
                    // If no last guess then draw and set country to last guess
                    if (lastGuess == null) {
                        lastGuess = country;
                        drawCountryByName(input);
                    }
                    // If we have a last guess then validate
                    else {
                        // Find neighbors and print for debugging
                        lastGuess.loadNeighbors(lastGuess.getName());
                        System.out.println("Loaded " + lastGuess.getNeighbors().size() + " neighbors for " + lastGuess.getName());
                        for (Country n : lastGuess.getNeighbors()) {
                            System.out.println("  [" + n.getName() + "]");
                        }
                        // If we find a match then draw and set last guess
                        if (lastGuess.isNeighbor(country)) {
                            lastGuess = country;
                            drawCountryByName(input);
                        }
                        // No match
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

    // Function that finds the continent a given country belongs in
    public static String[] findContinent(String countryName) {
        // Go through the dataset
        for (String[] continent : CONTINENT_COUNTRIES) {
            // Countries start at index three because first 3 are the continent, x rotation value, and y rotation value
            for (int i = 3; i < continent.length; i++) {
                // If we find a match then return the continent
                if (continent[i].equalsIgnoreCase(countryName)) {
                    return continent;
                }
            }
        }
        // No match
        return null;
    }

    // Function to draw the user's mistakes with red Xs
    public static void drawMistakeMarker(Pane rootPane) {
        // center X of the X shape and size
        double cx = 50 + (numMistakes - 1) * 40;
        double cy = 50;
        double size = 12;

        // Draw X with lines
        javafx.scene.shape.Line line1 = new javafx.scene.shape.Line(
                cx - size, cy - size, cx + size, cy + size
        );
        javafx.scene.shape.Line line2 = new javafx.scene.shape.Line(
                cx + size, cy - size, cx - size, cy + size
        );

        // Set color and thickness
        line1.setStroke(Color.RED);
        line2.setStroke(Color.RED);
        line1.setStrokeWidth(3);
        line2.setStrokeWidth(3);

        // Add to pane to draw
        rootPane.getChildren().addAll(line1, line2);
    }

    // Function that ends game either for loser or winner
    public static void showGameOver(Pane rootPane) {
        if (gameTimer != null) gameTimer.stop();
        // Clear the 3D globe countries
        globeGroup.getChildren().subList(1, globeGroup.getChildren().size()).clear();

        // Remove everything from rootPane except the SubScene
        rootPane.getChildren().subList(1, rootPane.getChildren().size()).clear();

        // If we lost then say that
        javafx.scene.text.Text text;
        if (validGuesses < 193) {
            // Big "YOU LOSE" text
            text = new javafx.scene.text.Text("YOU LOSE");
            text.setFill(Color.RED);
        }
        // If we won then say that
        else {
            // Big "YOU WON" text
            text = new javafx.scene.text.Text("YOU WIN!");
            text.setFill(Color.GREEN);
        }

        // Set font and size
        text.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 120));

        // Center it on screen
        text.setLayoutX(WIDTH / 2 - 310);
        text.setLayoutY(HEIGHT / 2 + 40);

        // Add to pane to draw
        rootPane.getChildren().add(text);
    }

    // Function to draw the country
    public static void drawCountryByName(String targetName) {
        // Get rings (these distinguish mainland from islands)
        ArrayList<Country> rings = Country.getRings(targetName);
        // If nothing found then no drawing
        if (rings == null) {
            System.out.println("Country not found: " + targetName);
            return;
        }
        // Draw country outline and fill
        System.out.println("Drawing: " + targetName + " (" + rings.size() + " ring(s))");
        Platform.runLater(() -> {
            drawCountry3DFilled(rings, Color.LIGHTGREEN);
            drawCountry3D(rings, Color.DARKCYAN);
        });
    }

    // Draws the outline of a country
    public static void drawCountry3D(ArrayList<Country> rings, Color color) {
        // Variable for lighting
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(color.brighter());

        // For all rings
        for (Country ring : rings) {
            // Get the points
            double[][] pts = ring.getPoints();
            // Step over the radius of a point so no overlap (more dense equals more step)
            int step = pts.length > 500 ? 3 : pts.length > 200 ? 2 : 1;
            // Go through points
            for (int k = 0; k < pts.length; k += step) {
                // Convert to XYZ so we can plot on globe
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

    // Fills in a country with ray casting algorithm
    public static void drawCountry3DFilled(ArrayList<Country> rings, Color color) {
        // Variables for lighting
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(color.brighter());

        // Bounding box across all rings — O(n_rings) using stored fields
        double minLat = rings.stream().mapToDouble(r -> r.minLat).min().orElse(-90);
        double maxLat = rings.stream().mapToDouble(r -> r.maxLat).max().orElse(90);
        double minLon = rings.stream().mapToDouble(r -> r.minLon).min().orElse(-180);
        double maxLon = rings.stream().mapToDouble(r -> r.maxLon).max().orElse(180);

        // Determines how close points are
        double step = 0.7;

        // Go through y then x
        for (double lat = minLat; lat <= maxLat; lat += step) {
            for (double lon = minLon; lon <= maxLon; lon += step) {
                // If find a point inside the country
                if (Country.isPointInAnyRing(lat, lon, rings)) {
                    // Convert to XYZ and plot
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

    /**
     * Parse a Polygon coordinates block: [ [ [lon,lat], ... ], ... ]
     * Depth 1 = outer array of rings, Depth 2 = a ring, Depth 3 = a coordinate pair
     */
    // Used by the Country class to parse the JSON
    public static ArrayList<double[][]> parsePolygon(String json) {
        // Get rings
        ArrayList<double[][]> rings = new ArrayList<>();
        int depth = 0;
        int ringStart = -1;

        // Go through json
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            // Find array of rings
            if (c == '[') {
                depth++;
                // Found a ring
                if (depth == 2) ringStart = i;
            }
            // End of ring
            else if (c == ']') {
                // Get coordinates
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
    // Also used by country class to parse JSON
    public static ArrayList<double[][]> parseMultiPolygon(String json) {
        // Variables for storing and tracking
        ArrayList<double[][]> polygons = new ArrayList<>();
        int depth = 0;
        int ringStart = -1;

        // Go through json
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            // Found something
            if (c == '[') {
                depth++;
                // Found a ring inside a polygon
                if (depth == 3) ringStart = i;
            }
            // End of ring
            else if (c == ']') {
                // Get coordinates
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
        // Variables for storing and tracking
        ArrayList<double[]> points = new ArrayList<>();
        int depth = 0;
        int pairStart = -1;

        // Go through json
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            // Found something
            if (c == '[') {
                depth++;
                // Found [lon, lat] pair
                if (depth == 2) pairStart = i;
            }
            // End of pair
            else if (c == ']') {
                // Get inner pair and trim
                if (depth == 2 && pairStart != -1) {
                    String inner = json.substring(pairStart + 1, i).trim();
                    String[] parts = inner.split(",");
                    if (parts.length >= 2) {
                        try {
                            // If in bounds then add
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

    // Function to convert lat/lon points to XYZ coordinates so they can be plotted on a globe
    public static double[] latLonToXYZ(double latDeg, double lonDeg, double R) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);
        double x =  -R * Math.cos(lat) * Math.sin(lon);
        double y = -R * Math.sin(lat);
        double z =  R * Math.cos(lat) * Math.cos(lon);
        return new double[]{x, y, z};
    }

    // Function to rotate globe to desired x angle and y angle
    public static void centerGlobeOn(double spinLeftRight, double tiltUpDown) {
        Platform.runLater(() -> {
            rotateY.setAngle(spinLeftRight);
            rotateX.setAngle(tiltUpDown);
        });
    }

    // Start
    public static void main(String[] args) {
        launch();
    }
}