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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Token other = (Token) obj;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return text + " - " + range;
    }
    
}
