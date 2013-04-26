package org.juxtasoftware;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Configuration.Hyphens;
import org.juxtasoftware.model.Range;
import org.juxtasoftware.model.Token;

/**
 * Break a witness into a stream of tokens
 * 
 * @author loufoster
 *
 */
public class Tokenizer {
    private static Logger LOG = Logger.getLogger(Tokenizer.class);
    private Configuration config;
    private List<Token> tokens;
    private static final Pattern PUNCTUATION = Pattern.compile("[^a-zA-Z0-9\\-]");

    private enum RunType {
        NONE, TOKEN, NON_TOKEN, WHITESPACE
    };
    
    /**
     * Set tokenizer configuration
     * @param cfg
     */
    public void setConfig(Configuration cfg) {
        this.config = cfg;
    }
    
    /**
     * Get the tokenizer token list
     * @return
     */
    public List<Token> getTokens() {
        return this.tokens;
    }

    /**
     * Break the content of the <code>srcFile</code> into tokens based upon
     * the configuration data held in <code>cfg</code>
     * 
     * @param cfg
     * @param srcFile
     * @return
     * @throws IOException
     */
    public List<Token> tokenize(final Reader srcReader) throws IOException {
        // reset the tokenizer results
        this.tokens = new ArrayList<Token>();
        
        // set up token tracking data
        RunType runType = RunType.NONE;
        long start = -1;
        long offset = -1;
        StringBuilder tokenText = new StringBuilder();

        LOG.info("Tokenizing witness");
        while (true) {
            final int read = srcReader.read();
            if (read < 0) {
                if (start != -1) {
                    createToken( start, offset, tokenText);
                }
                break;
            } else {
                
                // letter or digit?
                if (isTokenChar(read)) {
                    // this ends a prior run of non-token characters
                    if (runType.equals(RunType.NON_TOKEN)) {
                        createToken(start, offset, tokenText);
                        start = -1;
                        tokenText = new StringBuilder();
                    }
                    
                    // now it is a token run. track the start if it is new
                    runType = RunType.TOKEN;
                    if (start == -1 ) {
                        start = offset;
                    }
                    
                } else {
                    // This char is whitespace or punctuation
                   
                    // if this non-token char breaks up a prior token
                    // run, create a new token with it
                    if ( runType.equals(RunType.TOKEN) ) {
                        createToken( start, offset, tokenText);
                        start = -1;
                        runType = RunType.NONE;
                        tokenText = new StringBuilder();
                    } 
                    
                    // Start or continue a run of non-token characters?
                    if ( Character.isWhitespace(read) == false ) {
                        runType = RunType.NON_TOKEN;
                        if (start == -1 ) {
                            start = offset;
                        }
                    } else {
                        // This is whitespace. See if we need to end a non-token run.
                        // other than that, do not track the whitespace
                        if ( runType.equals(RunType.NON_TOKEN) ) {
                            createToken(start, offset, tokenText);
                            runType = RunType.NONE;
                            start = -1;
                            tokenText = new StringBuilder();
                        }
                    }
                }
            }

            tokenText.append((char) read);
            offset++;
        }

        return tokens;
    }

    private void createToken(long start, long offset, StringBuilder tokenText) {
        String txt = tokenText.toString();
        Range r = new Range(start, (offset - start));
        
        // try to identify linebreak hyphenation
        boolean joinHyphenated = false;
        if ( this.config.getHyphenation().equals(Hyphens.LINEBREAK) && this.tokens.size()>=2 ) {
            Token lastToken = this.tokens.get( this.tokens.size()-1);
            if ( lastToken.getText().equals("-")) {
                joinHyphenated = txt.contains("\n");
            }
        }

        // normalize based on config
        if ( this.config.isIgnoreCase() ) {
            txt = txt.toLowerCase();
        } 
        if ( this.config.isIgnorePunctuation()) {
            txt = PUNCTUATION.matcher(txt).replaceAll("");
        }
        if ( this.config.getHyphenation().equals(Hyphens.NONE) ) {
            txt = txt.replaceAll("-", "");
        }
        txt = txt.trim().replaceAll("\\s+", " ");
        
        // add it to the list if there is anything left
        if ( txt.length() > 0 ) {
            if ( joinHyphenated) {
                // toss the hyphen and grab the preceeding token 
                this.tokens.remove(this.tokens.size()-1);
                Token prior = this.tokens.remove(this.tokens.size()-1);
                txt = prior.getText() + txt;
            }
            this.tokens.add( new Token(r, txt));
        }
    }

    private boolean isTokenChar(int c) {
        if (Character.isLetter(c) || Character.isDigit(c) ) {
            return true;
        }
        return false;
    }
}