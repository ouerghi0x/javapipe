package com.example;

import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("=== Running Maven Project with Java Pipe ===");

        // Functional pipeline using |> directly inside standard Maven project
        var rawInput = "   42, 18, -99, 105, -7   ";

        var processedNumbers = List.of(rawInput.trim().split(",\\s*"))
            |> stream()
            |> map(Integer::parseInt)
            |> filter(n -> n > 0)
            |> sorted()
            |> toList();

        System.out.println("Input: " + rawInput);
        System.out.println("Positive Sorted Numbers: " + processedNumbers);

        // String pipeline
        var message = "hello maven world"
            |> String::toUpperCase
            |> msg -> ">>> " + msg + " <<<";

        System.out.println(message);
    }
}
