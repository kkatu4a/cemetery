package com.arnor4eck;

import com.arnor4eck.Application;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        try(Scanner scanner = new Scanner(System.in)) {
            Application app = new Application(scanner);
            app.run();
        }
    }
}