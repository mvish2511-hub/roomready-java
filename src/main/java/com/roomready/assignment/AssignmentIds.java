package com.roomready.assignment;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** An assignment's public id - AS118 - made from its database id, which is never shown itself. */
public final class AssignmentIds {

    private static final Pattern PUBLIC = Pattern.compile("AS(\\d+)");

    private AssignmentIds() {
    }

    public static String format(long id) {
        return String.format("AS%03d", id);
    }

    /** The database id behind a public id, or empty when it is not one. */
    public static Optional<Long> parse(String publicId) {
        Matcher m = PUBLIC.matcher(publicId);
        return m.matches() ? Optional.of(Long.parseLong(m.group(1))) : Optional.empty();
    }
}
