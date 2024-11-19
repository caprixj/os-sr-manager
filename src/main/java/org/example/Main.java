package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Manager manager = new Manager(8080);
            manager.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}