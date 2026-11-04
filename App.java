package com.example;

import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("=== Running Gradle Project with Java Pipe ===");

        // Functional pipeline in a Gradle project
        var numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        var sumOfSquares = numbers.stream().filter(n -> n % 2 == 0).map(n -> n * n).mapToInt(Integer::intValue).sum();

        System.out.println("Even Numbers Squared Sum: " + sumOfSquares);

        // String pipeline
        var banner = __pipe(__pipe("powered by java pipe and gradle", String::toUpperCase), text -> "🚀 [ " + text + " ] 🚀");

        System.out.println(banner);
    }

    // Synthesized by javapipec for functional pipe expressions
    private static <__T, __R> __R __pipe(__T __val, java.util.function.Function<__T, __R> __fn) {
        return __fn.apply(__val);
    }
}
