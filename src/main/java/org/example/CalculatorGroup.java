package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CalculatorGroup {
    private final Map<String, CalculatorComponent> components;
    private final double x;

    public CalculatorGroup(double x) {
        this.x = x;
        this.components = new HashMap<>();
    }

    public void addComponent(CalculatorComponent component) {
        components.put(component.getName(), component);
    }

    public Future<Map<String, Double>> compute() {
        CompletableFuture<Map<String, Double>> future = new CompletableFuture<>();
        Map<String, Double> results = new HashMap<>();

        for (Map.Entry<String, CalculatorComponent> entry : components.entrySet()) {
            String componentName = entry.getKey();
            Future<Double> resultFuture = entry.getValue().calculate(x);

            CompletableFuture<Double> completableResultFuture = toCompletableFuture(resultFuture);

            completableResultFuture.thenAccept(result -> {
                synchronized (results) {
                    results.put(componentName, result);
                    if (results.size() == components.size()) {
                        future.complete(results);
                    }
                }
            }).exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        }

        return future;
    }

    public Map<String, CalculatorComponent> getComponents() {
        return components;
    }

    private static <T> CompletableFuture<T> toCompletableFuture(Future<T> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
