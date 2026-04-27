package org.example.demo1;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.ArrayList;

public class Country {
    private String name;
    private double[][] location;
    private ArrayList<Country> polygons; // each element is one ring/island
    private ArrayList<Country> neighbors;

    public Country(String name) {
        this.name = name;
        this.polygons = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Country> getNeighbors() {
        return this.neighbors;
    }

    // Makes a country object for each country, initializing the neighbors array with data from csv file
    // File name: /Users/lilykassaei/IdeaProjects/demo1/src/main/resources/GEODATASOURCE-COUNTRY-BORDERS.CSV
    // Ex of csv file:
    // "country_code","country_name","country_border_code","country_border_name"
    //"AD","Andorra","FR","France"
    //"AD","Andorra","ES","Spain"
    //"AE","United Arab Emirates","OM","Oman"
    //"AE","United Arab Emirates","SA","Saudi Arabia"
    public ArrayList<Country> loadNeighbors(String name) {

        try {
            InputStream is = Country.class.getClassLoader().getResourceAsStream("GEODATASOURCE-COUNTRY-BORDERS.CSV");
            String csv = new String(is.readAllBytes());

            String[] lines = csv.split("\\r?\\n");

            for (String line : lines) {
                // Remove quotes and split by comma
                line = line.replace("\"", "");
                String[] parts = line.split(",");

                // parts[0] = country_code
                // parts[1] = country_name
                // parts[2] = border_code
                // parts[3] = border_name
                if (parts.length < 4) {
                    continue;
                }

                String countryName = parts[1].trim();
                String neighborName = parts[3].replace("\r", "").trim();

                // If this row matches our country, add the neighbor
                if (countryName.equalsIgnoreCase(name)) {
                    this.neighbors.add(new Country(neighborName));
                }
            }

        } catch (Exception e) {
            System.out.println("Error loading neighbors: " + e.getMessage());
        }

        return neighbors;
    }

    public boolean isNeighbor(Country country) {
        for (Country n : neighbors) {
            if (n.getName().equalsIgnoreCase(country.getName())) {
                return true;
            }
        }
        return false;
    }


}
