package org.juxtasoftware.model;

/**
 * A single token as determined by the tokenizer. Includes an offset-range pair
 * and the token text
 * @author loufoster
 *
 */
public class Token {
    private final Range range;
    private final String text;
    
    public Token(final Range r, final String txt) {
        this.range = r;
        this.text = txt;
    }

    public Range getRange() {
        return range;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Token [range=" + range + ", text=" + text + "]";
    }
    
}
