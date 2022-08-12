package dev.cerus.edina.ast.exception;

import dev.cerus.edina.ast.token.Location;
import java.util.Arrays;
import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.Color.WHITE;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;

/**
 * Base class for ast exceptions
 */
public abstract class LocatedException extends RuntimeException {

    private final Location location;

    public LocatedException(final String message, final Location location) {
        super(message);
        this.location = location;
    }

    public LocatedException(final String message, final Throwable cause, final Location location) {
        super(message, cause);
        this.location = location;
    }

    /**
     * Print the error details in a nice human-readable fashion
     */
    public void printDetailedError() {
        final AnsiPrintStream stdOut = AnsiConsole.out();

        // Separator
        stdOut.println(ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).a(" ".repeat(48)).reset());

        // Print msg and causes
        stdOut.println(ansi()
                .fgBrightRed()
                .a(Ansi.Attribute.INTENSITY_BOLD)
                .a(this.getMessage()).reset());
        Throwable cause = this.getCause();
        while (cause != null) {
            System.out.println(" Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
        }
        System.out.println();

        // Print affected lines
        stdOut.println(ansi().a(Ansi.Attribute.ITALIC).a("File " + this.loc().fileName() + ":").reset());
        if (this.loc().lines().length >= 1) {
            final int startLen = this.loc().firstLine().length();
            for (int li = 0; li < this.loc().lines().length; li++) {
                final String line = this.loc().lines()[li];
                final boolean start = li == 0;
                final boolean end = li == this.loc().lines().length - 1;
                final String lineNumTxt = String.format("L%-3s", (li + this.loc().fromLineNum()));

                // Initialize the text we're going to print
                Ansi ansiLine = ansi()
                        .bgRgb(100, 100, 100) // Light gray background
                        .fgRgb(255, 255, 255) // Black foreground
                        .a(lineNumTxt) // Formatted line number
                        .reset()
                        .a("    ");
                if (start && end) {
                    // Start and end of the error is in this line
                    ansiLine = ansiLine.a(line.substring(0, this.loc().from())) // First part of string is not part of the error, print normally
                            .bgBrightRed() // Red background
                            .fg(WHITE) // White foreground
                            .a(line.substring(this.loc().from(), this.loc().to())) // This part of string is part of the error, print red
                            .reset() // Reset everything
                            .a(line.substring(this.loc().to())); // Last part of string is not part of error, print normally
                    ansiLine = ansiLine.reset().newline()
                            .a(" ".repeat(lineNumTxt.length() + 4 + this.loc().from()))
                            .fg(Ansi.Color.RED)
                            .a("^".repeat(this.loc().to() - this.loc().from()));
                } else if (start) {
                    // Start of the error is in this line
                    ansiLine = ansiLine.a(line.substring(0, this.loc().from())) // First part of string is not part of the error, print normally
                            .bgBrightRed() // Red background
                            .fg(WHITE) // White foreground
                            .a(line.substring(this.loc().from())); // This part of string is part of the error, print red
                } else if (end) {
                    // End of the error is in this line
                    ansiLine = ansiLine.bgBrightRed() // Red background
                            .fg(WHITE) // White foreground
                            .a(line.substring(0, this.loc().to())) // This part of string is part of the error, print red
                            .reset() // Reset everything
                            .a(line.substring(this.loc().to())); // Last part of string is not part of error, print normally

                    int len = Arrays.stream(this.loc().lines(), 1, this.loc().lines().length - 1)
                            .mapToInt(String::length)
                            .max()
                            .orElse(1);
                    len = Math.max(len, Math.max(this.loc().firstLine().length(), this.loc().to()));
                    ansiLine = ansiLine.reset().newline()
                            .a(" ".repeat(lineNumTxt.length() + 4))
                            .fg(Ansi.Color.RED)
                            .a("^".repeat(len));
                } else {
                    // Whole line is erroneous
                    ansiLine = ansiLine.bgBrightRed().fg(WHITE).a(line); // Print line in red
                    if (line.length() < startLen) {
                        ansiLine = ansiLine.a(" ".repeat(startLen - line.length())); // Pad with space to match start line
                    }
                    ansiLine = ansiLine.reset();
                }
                // Print our constructed line
                stdOut.println(ansiLine.reset());
            }
        }

        // Separator
        stdOut.println(ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).a(" ".repeat(48)).reset());
    }

    private Location loc() {
        return this.getLocation();
    }

    public Location getLocation() {
        return this.location;
    }

}
