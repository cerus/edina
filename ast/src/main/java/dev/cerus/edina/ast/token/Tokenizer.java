package dev.cerus.edina.ast.token;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms script content into parser-readable bits by splitting the content into tokens
 */
public class Tokenizer {

    private final List<String> lines;
    private int lineNum;
    private char[] line;
    private int charNum;

    public Tokenizer(final List<String> lines) {
        this.lines = lines;
    }

    /**
     * Tokenize the script
     *
     * @return A list of parsed tokens
     */
    public List<Token> tokenize() {
        final List<Token> tokens = new ArrayList<>();
        line_loop:
        while (this.lineNum < this.lines.size()) {
            this.line = this.lines.get(this.lineNum++).toCharArray();
            this.charNum = 0;
            while (this.charNum < this.line.length) {
                // Skip comments
                if (this.line[this.charNum] == '#' && (this.charNum == 0 || this.line[this.charNum - 1] != '\\')) {
                    continue line_loop;
                }

                final Token token = this.getToken(this.line[this.charNum]);
                if (token != null) {
                    tokens.add(token);
                }
                this.charNum++;
            }
        }
        return tokens;
    }

    /**
     * Parse a token from a char
     * <p>
     * This function will advance the charNum variable arbitrarily depending on what it's trying to parse.
     *
     * @param c The character to parse
     *
     * @return A parsed token or null
     */
    private Token getToken(final char c) {
        final Token simpleToken = this.getSimpleToken(c);
        if (simpleToken != null) {
            return simpleToken;
        }

        if (!Character.isWhitespace(c)) {
            final int start = this.charNum;
            int end = this.charNum;

            final StringBuilder builder = new StringBuilder();
            while (this.charNum < this.line.length
                    && !Character.isWhitespace(this.line[this.charNum])
                    && !this.isSimpleToken(this.line[this.charNum])) {
                builder.append(this.line[this.charNum]);
                this.charNum++;
                end++;
            }
            this.charNum--;
            return Token.of(TokenType.WORD, this.lineNum, start, end, builder.toString());
        } else {
            return null;
        }
    }

    /**
     * Attempt to get a matching simple token for a character
     *
     * @param c The character to get the simple token from
     *
     * @return A simple token if one matches, else null
     */
    private Token getSimpleToken(final char c) {
        final TokenType type = TokenType.getSimple(c);
        if (type == null) {
            return null;
        }
        if (type == TokenType.MINUS && this.charNum < this.line.length - 1 && Character.isDigit(this.line[this.charNum + 1])) {
            return null;
        }
        return Token.of(type, this.lineNum, this.charNum, this.charNum + 1, String.valueOf(c));
    }

    /**
     * Check if a character is a simple token
     *
     * @param c The character to check
     *
     * @return True if the character is a simple token, otherwise false
     */
    private boolean isSimpleToken(final char c) {
        final int i = this.charNum;
        final Token normalToken = this.getSimpleToken(c);
        this.charNum = i;
        return normalToken != null;
    }

}
