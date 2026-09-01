package com.gameoflife;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PatternCatalog {

    private final Map<String, Supplier<Grid>> patterns = new LinkedHashMap<>();

    public PatternCatalog() {
        patterns.put("Glider", Grid::glider);
        patterns.put("Pulsar", Grid::pulsar);
        patterns.put("Gosper Glider Gun", Grid::gosperGliderGun);
        patterns.put("Random (30%)", () -> {
            Grid g = new Grid(20, 40);
            g.randomize(0.30);
            return g;
        });
        patterns.put("Random (50%)", () -> {
            Grid g = new Grid(20, 40);
            g.randomize(0.50);
            return g;
        });
    }

    public Map<String, Supplier<Grid>> all() {
        return Map.copyOf(patterns);
    }

    public Grid create(String name) {
        var supplier = patterns.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown pattern: " + name);
        }
        return supplier.get();
    }

    public String[] names() {
        return patterns.keySet().toArray(String[]::new);
    }
}
