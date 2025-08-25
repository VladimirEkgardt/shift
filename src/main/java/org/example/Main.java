package org.example;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> utilities = new ArrayList<>(Arrays.stream(args).toList());

        String defaultIntegerFile = "integers.txt";
        String defaultFloatsFile = "floats.txt";
        String defaultStringFile = "strings.txt";
        String pathFile = "";
        boolean optionS = false;
        boolean optionF = false;
        boolean optionA = false;

        if (utilities.contains("-p")) {
            String prefix = utilities.get(utilities.indexOf("-p") + 1);

            defaultStringFile = prefix + defaultStringFile;
            defaultFloatsFile = prefix + defaultFloatsFile;
            defaultIntegerFile = prefix + defaultIntegerFile;

            utilities.remove(prefix);
            utilities.remove("-p");
        }

        if (utilities.contains("-o")) {
            pathFile = utilities.get(utilities.indexOf("-o") + 1);

            utilities.remove(pathFile);
            utilities.remove("-o");
        }

        if (utilities.contains("-s")) {
            optionS = true;
            utilities.remove("-s");
        }

        if (utilities.contains("-f")) {
            optionF = true;
            utilities.remove("-f");
        }

        if (utilities.contains("-a")) {
            optionA = true;
            utilities.remove("-a");
        }

        if (!utilities.stream().allMatch(file -> file.matches("^[a-z0-9]*.txt$"))) {
            throw new IllegalArgumentException("Error in arguments: " +
                    utilities.stream().filter(file -> !file.matches("[a-z0-9]*.txt")).toList());
        }

        List<String> integers = new ArrayList<>();
        List<String> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for (String file : utilities) {
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (reader.ready()) {
                    String str = reader.readLine();
                    if (isInteger(str)) {
                        integers.add(str);
                    } else if (isFloat(str)) {
                        floats.add(str);
                    } else {
                        strings.add(str);
                    }
                }
            } catch (FileNotFoundException e){
                System.err.println("File not found: " + e.getMessage());
            } catch (IOException e){
                System.err.println("Input error: " + e.getMessage());
            } catch (Exception e){
                System.err.println("Another error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        if (!integers.isEmpty()) {
            printToFile(integers, pathFile + defaultIntegerFile, optionA);
        }

        if (!floats.isEmpty()) {
            printToFile(floats, pathFile + defaultFloatsFile, optionA);
        }

        if (!strings.isEmpty()) {
            printToFile(strings, pathFile + defaultStringFile, optionA);
        }

        if (optionS) {
            printShortStatistic(integers.size(), floats.size(), strings.size());
        }

        if (optionF) {
            printFullStatistic(integers, floats, strings);
        }
    }

    private static boolean isFloat(String str) {
        try {
            return !(Float.parseFloat(str) < Float.MIN_VALUE) && !(Float.parseFloat(str) > Float.MAX_VALUE);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void printToFile(List<String> list, String fileName, boolean append) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, append))) {
            list.forEach(i -> {
                try {
                    writer.write(String.valueOf(i));
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch(IOException e){
            System.err.println("Output error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void printShortStatistic(int countInt, int countFloat, int countString) {
        System.out.println("Integers: " + countInt);
        System.out.println("Floats: " + countFloat);
        System.out.println("Strings: " + countString);
    }

    private static void printFullStatistic(List<String> integers, List<String> floats, List<String> strings) {
        if (!integers.isEmpty()) {
            int intMin = integers.parallelStream()
                    .mapToInt(Integer::parseInt)
                    .min()
                    .orElse(Integer.MIN_VALUE);
            int intMax = integers.parallelStream()
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(Integer.MAX_VALUE);
            int intSum = integers.parallelStream()
                    .mapToInt(Integer::parseInt)
                    .sum();

            System.out.println("Integers: count = " + integers.size() +
                    ", min = " + intMin +
                    ", max = " + intMax +
                    ", sum = " + intSum +
                    ", average = " + (double) intSum / integers.size());
        }

        if (!floats.isEmpty()) {
            BigDecimal floatMin = floats.parallelStream()
                    .map(BigDecimal::new)
                    .min(BigDecimal::compareTo)
                    .get();

            BigDecimal floatMax = floats.parallelStream()
                    .map(BigDecimal::new)
                    .max(BigDecimal::compareTo)
                    .get();

            BigDecimal floatSum = new BigDecimal("0.0");

            for (String fl : floats) {
                floatSum = floatSum.add(new BigDecimal(fl));
            }

            System.out.println("Floats: count = " + floats.size() +
                    ", min = " + floatMin +
                    ", max = " + floatMax +
                    ", sum = " + floatSum +
                    ", average = " + floatSum.divide(new BigDecimal(floats.size()), RoundingMode.HALF_UP));
        }

        if (!strings.isEmpty()) {
            int countMaxString = strings.parallelStream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(Integer.MAX_VALUE);

            int countMinString = strings.parallelStream()
                    .mapToInt(String::length)
                    .min()
                    .orElse(Integer.MIN_VALUE);

            System.out.println("Strings: count = " + strings.size() +
                    ", max = " + countMaxString +
                    ", min = " + countMinString);
        }
    }
}