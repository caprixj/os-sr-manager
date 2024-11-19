package org.example;

import java.util.concurrent.Future;

public interface CalculatorComponent {
    String getName();
    Future<Double> calculate(double x);
}
