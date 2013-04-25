package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Token;
import org.juxtasoftware.model.Configuration.Hyphens;

/**
 * Tests for the tokenizer
 * 
 * @author loufoster
 *
 */
public class TokenizerTest {

    @Test
    public void simpleTest() throws IOException {
        String txt = "The quick brown fox!";
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.ALL);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<Token> tokens = tokenizer.getTokens();
        
        assertTrue("Wrong number of tokens", tokens.size()==4);
        assertTrue("missing token 'the'", tokens.size()==4);
        assertTrue("missing token 'quick'", tokens.size()==4);
        assertTrue("missing token 'brown'", tokens.size()==4);
        assertTrue("missing token 'fox'", tokens.size()==4);
    }
}
