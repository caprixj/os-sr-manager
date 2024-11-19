package org.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SimpleCalculatorComponent implements CalculatorComponent {
    private final String name;

    public SimpleCalculatorComponent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Future<Double> calculate(double x) {
        CompletableFuture<Double> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                // Деяка проста обчислювана функція
                double result = Math.sin(x) + Math.cos(x);

                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).start();
        return future;
    }
}
