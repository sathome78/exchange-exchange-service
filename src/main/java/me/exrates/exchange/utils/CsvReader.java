package me.exrates.exchange.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class CsvReader {

    public static final String CSV_DELIMITER = ";";

    public static <T> Set<T> readAndMap(InputStream source, Function<String[], T> mapper) {
        Set<T> items = new HashSet<>();

        Scanner scanner = new Scanner(source);
        while (scanner.hasNext()) {
            String fileLine = scanner.nextLine();
            String[] line = parseLine(fileLine);
            T entry = mapper.apply(line);
            items.add(entry);
        }

        return items;
    }

    private static String[] parseLine(String line) {
        return line.split(CSV_DELIMITER);
    }
}
