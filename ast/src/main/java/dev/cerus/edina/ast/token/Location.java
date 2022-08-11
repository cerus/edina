package dev.cerus.edina.ast.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a location in the source text.
 *
 * @param lines       The selected lines
 * @param fromLineNum The start line number
 * @param toLineNum   The end line number
 * @param from        The start character index
 * @param to          The end character index
 */
public record Location(String[] lines, int fromLineNum, int toLineNum, int from, int to) {

    public static Location singleLine(final String line, final int lineNum, final int from, final int to) {
        return new Location(new String[] {line}, lineNum, lineNum, from, to);
    }

    public static Location multipleLines(final String[] lines, final int fromLineNum, final int toLineNum, final int from, final int to) {
        return new Location(lines, fromLineNum, toLineNum, from, to);
    }

    public static Location combine(final Location loc1, final Location loc2) {
        return combine(null, loc1, loc2);
    }

    public static Location combine(final List<String> source, final Location loc1, final Location loc2) {
        if (loc1.fromLineNum == loc2.fromLineNum && loc1.toLineNum == loc2.toLineNum) {
            return singleLine(loc1.firstLine(), loc1.fromLineNum, Math.min(loc1.from, loc2.from), Math.max(loc1.to, loc2.to));
        }

        final List<String> lineList = new ArrayList<>();
        if (source == null) {
            lineList.addAll(Arrays.asList(loc1.lines));
            lineList.addAll(Arrays.asList(loc2.lines));
        } else {
            for (int li = Math.min(loc1.fromLineNum, loc2.fromLineNum) - 1; li <= Math.max(loc1.toLineNum, loc2.toLineNum) - 1; li++) {
                lineList.add(source.get(li));
            }
        }

        return new Location(
                lineList.toArray(new String[0]),
                Math.min(loc1.fromLineNum, loc2.fromLineNum),
                Math.max(loc1.toLineNum, loc2.toLineNum),
                (loc1.fromLineNum <= loc2.fromLineNum ? loc1 : loc2).from,
                (loc1.fromLineNum >= loc2.fromLineNum ? loc1 : loc2).to
        );
    }

    public Location combineWith(final Location other) {
        return combine(this, other);
    }

    public Location combineWith(final List<String> source, final Location other) {
        return combine(source, this, other);
    }

    public String firstLine() {
        return this.lines()[0];
    }

    public String lastLine() {
        return this.lines()[this.lines.length - 1];
    }

}
