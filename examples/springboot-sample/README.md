# Spring Boot 3 + Java Pipe (`|>`) Sample

This sample application demonstrates how to use the **Java Pipe (`|>`)** functional operator inside a production **Spring Boot 3** REST API.

---

## 🚀 Features Demonstrated

### 1. String Pipeline (`/api/transform`)
```java
@GetMapping("/transform")
public Map<String, Object> transformText(@RequestParam String text) {
    var processed = text
        |> String::trim
        |> str -> str.replace("-", " ")
        |> String::toUpperCase
        |> str -> "✨ " + str + " ✨";

    return Map.of("result", processed);
}
```

### 2. Stream Data Processing (`/api/products`)
```java
@GetMapping("/products")
public List<Product> getProducts(@RequestParam String category) {
    return catalog
        |> stream()
        |> filter(Product::available)
        |> filter(p -> category == null || p.category().equalsIgnoreCase(category))
        |> sorted((p1, p2) -> Double.compare(p1.price(), p2.price()))
        |> toList();
}
```

### 3. Number Filtering & Aggregation (`/api/sum`)
```java
@GetMapping("/sum")
public Map<String, Object> sumPositiveNumbers(@RequestParam String numbers) {
    var total = List.of(numbers.split(","))
        |> stream()
        |> map(String::trim)
        |> map(Integer::parseInt)
        |> filter(n -> n > 0)
        |> mapToInt(Integer::intValue)
        |> sum();

    return Map.of("positiveSum", total);
}
```

---

## 🏃 Running the Application

### Using Maven:

```bash
# From this directory:
mvn spring-boot:run
```

Or package a standalone executable fat JAR:

```bash
mvn clean package
java -jar target/springboot-javapipe-sample-1.0.0.jar
```

---

## 🧪 Testing the REST Endpoints

Once running on port `8080`, test the endpoints:

#### 1. String Transformation Pipeline
```bash
curl "http://localhost:8080/api/transform?text=hello-spring-boot-with-java-pipe"
```
**Response:**
```json
{
  "original": "hello-spring-boot-with-java-pipe",
  "pipedResult": "✨ HELLO SPRING BOOT WITH JAVA PIPE ✨"
}
```

#### 2. Products Stream Pipeline
```bash
curl "http://localhost:8080/api/products?category=AUDIO"
```
**Response:**
```json
[
  { "id": "P103", "name": "Sony WH-1000XM5", "category": "AUDIO", "price": 349.99, "available": true },
  { "id": "P104", "name": "Bose QC Ultra", "category": "AUDIO", "price": 399.0, "available": true }
]
```

#### 3. Positive Numbers Sum Pipeline
```bash
curl "http://localhost:8080/api/sum?numbers=10,25,-5,40,-12,100"
```
**Response:**
```json
{
  "input": "10,25,-5,40,-12,100",
  "positiveSum": 175
}
```

