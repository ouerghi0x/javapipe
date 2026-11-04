package com.example.demo.controller;

import com.example.demo.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PipeDemoController {

    private final List<Product> catalog = List.of(
        new Product("P101", "MacBook Pro M3", "LAPTOPS", 1999.99, true),
        new Product("P102", "Dell XPS 15", "LAPTOPS", 1499.00, false),
        new Product("P103", "Sony WH-1000XM5", "AUDIO", 349.99, true),
        new Product("P104", "Bose QC Ultra", "AUDIO", 399.00, true),
        new Product("P105", "Logitech MX Master 3S", "ACCESSORIES", 99.99, true)
    );

    /**
     * Demonstrates string transformations with |> operator.
     * Example: /api/transform?text=hello-spring-boot-with-java-pipe
     */
    @GetMapping("/transform")
    public Map<String, Object> transformText(@RequestParam(defaultValue = "  spring-boot-with-java-pipe  ") String text) {
        var processed = text
            |> String::trim
            |> str -> str.replace("-", " ")
            |> String::toUpperCase
            |> str -> "✨ " + str + " ✨";

        return Map.of(
            "original", text,
            "pipedResult", processed
        );
    }

    /**
     * Demonstrates stream filtering and sorting with |> operator.
     * Example: /api/products?category=AUDIO
     */
    @GetMapping("/products")
    public List<Product> getProducts(@RequestParam(required = false) String category) {
        var filtered = catalog
            |> stream()
            |> filter(Product::available)
            |> filter(p -> category == null || p.category().equalsIgnoreCase(category))
            |> sorted((p1, p2) -> Double.compare(p1.price(), p2.price()))
            |> toList();

        return filtered;
    }

    /**
     * Demonstrates numerical pipeline with |> operator.
     * Example: /api/sum?numbers=10,25,-5,40,-12,100
     */
    @GetMapping("/sum")
    public Map<String, Object> sumPositiveNumbers(@RequestParam(defaultValue = "10,25,-5,40,-12,100") String numbers) {
        var total = List.of(numbers.split(","))
            |> stream()
            |> map(String::trim)
            |> map(Integer::parseInt)
            |> filter(n -> n > 0)
            |> mapToInt(Integer::intValue)
            |> sum();

        return Map.of(
            "input", numbers,
            "positiveSum", total
        );
    }
}

