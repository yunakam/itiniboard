package com.initiboard.api.util;

import java.util.function.Predicate;

public final class CopyNameGenerator {

    private CopyNameGenerator() {
    }

    public static String generate(String sourceName, Predicate<String> nameExists) {
        String baseName = sourceName.replaceFirst(" \\(\\d+\\)$", "");

        int copyNumber = 1;
        String candidateName;

        do {
            candidateName = baseName + " (" + copyNumber + ")";
            copyNumber++;
        } while (nameExists.test(candidateName));

        return candidateName;
    }
}