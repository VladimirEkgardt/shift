package org.example;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class DataFilter {
    private static final String DEFAULT_NAME_INTEGERS_FILE = "integers.txt";
    private static final String DEFAULT_NAME_FLOATS_FILE = "floats.txt";
    private static final String DEFAULT_NAME_STRING_FILE = "strings.txt";

    public static void main(String[] args) {
        List<String> arguments = new ArrayList<>(Arrays.stream(args).toList());

        String pathFile = "";
        String prefix = "";
        boolean shortStatistics = false;
        boolean fullStatistics = false;
        boolean addingToExistingFiles = false;

        if (arguments.contains("-p")) {
            if (arguments.indexOf("-p") >= (arguments.size() - 1)) {
                System.err.println("Incorrect prefix setting");
            } else {
                prefix = arguments.get(arguments.indexOf("-p") + 1);

                if (prefix.startsWith("-")) {
                    System.err.println("The prefix cannot start with -. You may not have specified a prefix");
                    prefix = "";
                } else {
                    arguments.remove(prefix);
                }
            }
            arguments.remove("-p");
        }

        if (arguments.contains("-o")) {
            if (arguments.indexOf("-o") >= (arguments.size() - 1)) {
                System.err.println("Incorrect path setting");
            } else {
                pathFile = arguments.get(arguments.indexOf("-o") + 1);

                if (pathFile.startsWith("-")) {
                    System.err.println("The path cannot start with -. You may not have specified a path");
                    pathFile = "";
                } else {
                    arguments.remove(pathFile);
                }
            }
            arguments.remove("-o");
        }

        if (arguments.contains("-s")) {
            shortStatistics = true;
            arguments.remove("-s");
        }

        if (arguments.contains("-f")) {
            fullStatistics = true;
            arguments.remove("-f");
        }

        if (arguments.contains("-a")) {
            addingToExistingFiles = true;
            arguments.remove("-a");
        }

        List<String> integers = new ArrayList<>();
        List<String> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for (String file : arguments) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isInteger(line)) {
                        integers.add(line);
                    } else if (isFloat(line)) {
                        floats.add(line);
                    } else {
                        strings.add(line);
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Input error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Another error: " + e.getMessage());
            }
        }

        if (!integers.isEmpty()) {
            printToFile(integers, buildPathFile(pathFile, prefix, DEFAULT_NAME_INTEGERS_FILE), addingToExistingFiles);
        }

        if (!floats.isEmpty()) {
            printToFile(floats, buildPathFile(pathFile, prefix, DEFAULT_NAME_FLOATS_FILE), addingToExistingFiles);
        }

        if (!strings.isEmpty()) {
            printToFile(strings, buildPathFile(pathFile, prefix, DEFAULT_NAME_STRING_FILE), addingToExistingFiles);
        }

        if (fullStatistics) {
            printFullStatistic(integers, floats, strings);
        } else {
            if (shortStatistics) {
                printShortStatistic(integers.size(), floats.size(), strings.size());
            }
        }

    }

    private static boolean isFloat(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isInteger(String str) {
        try {
            new BigInteger(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String buildPathFile(String path, String prefix, String defaultName) {
        if (path.isEmpty()) {
            return prefix + defaultName;
        }
        return path.endsWith(File.separator) ?
                path + prefix + defaultName :
                path + File.separator + prefix + defaultName;
    }

    private static void printToFile(List<String> list, String fileName, boolean append) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, append));
            list.forEach(i -> {
                try {
                    writer.write(String.valueOf(i));
                    writer.newLine();
                } catch (IOException e) {
                    System.err.println("Warning: Failed to write item to " + fileName + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Output error: " + e);
        }
    }

    private static void printShortStatistic(int countInt, int countFloat, int countString) {
        System.out.println("Integers: count = " + countInt);
        System.out.println("Floats: count = " + countFloat);
        System.out.println("Strings: count = " + countString);
    }

    private static void printFullStatistic(List<String> integers, List<String> floats, List<String> strings) {
        if (!integers.isEmpty()) {
            BigInteger intMin = getMinMaxSumElement(integers, BigInteger::new,
                    (a, b) -> a.compareTo(b) < 0 ? a : b);
            BigInteger intMax = getMinMaxSumElement(integers, BigInteger::new,
                    (a, b) -> a.compareTo(b) > 0 ? a : b);
            BigInteger intSum = getMinMaxSumElement(integers, BigInteger::new,
                    BigInteger::add);

            System.out.println("Integers: count = " + integers.size() +
                    ", min = " + intMin +
                    ", max = " + intMax +
                    ", sum = " + intSum +
                    ", average = " + intSum.divide(BigInteger.valueOf(integers.size())));
        } else {
            System.out.println("Integers: count = " + 0);
        }

        if (!floats.isEmpty()) {
            BigDecimal floatMin = getMinMaxSumElement(floats, BigDecimal::new, (a, b) -> a.compareTo(b) < 0 ? a : b);
            BigDecimal floatMax = getMinMaxSumElement(floats, BigDecimal::new, (a, b) -> a.compareTo(b) > 0 ? a : b);
            BigDecimal floatSum = getMinMaxSumElement(floats, BigDecimal::new, BigDecimal::add);

            System.out.println("Floats: count = " + floats.size() +
                    ", min = " + floatMin +
                    ", max = " + floatMax +
                    ", sum = " + floatSum +
                    ", average = " + floatSum.divide(new BigDecimal(floats.size()), RoundingMode.HALF_UP));
        } else {
            System.out.println("Floats: count = " + 0);
        }

        if (!strings.isEmpty()) {
            int countMinString = getMinMaxSumElement(strings, String::length, Math::min);
            int countMaxString = getMinMaxSumElement(strings, String::length, Math::max);

            System.out.println("Strings: count = " + strings.size() +
                    ", min = " + countMinString +
                    ", max = " + countMaxString);
        } else {
            System.out.println("Strings: count = " + 0);
        }
    }

    private static <T> T getMinMaxSumElement(List<String> listItems, Function<String, T> mapper,
                                             BinaryOperator<T> operator) {
        return listItems.stream()
                .map(mapper)
                .reduce(operator)
                .orElseThrow(() -> new IllegalStateException("Could not find value"));
    }
}