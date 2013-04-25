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
        assertTrue("missing token 'the'", tokens.get(0).getText().equals("the"));
        assertTrue("missing token 'quick'", tokens.get(1).getText().equals("quick"));
        assertTrue("missing token 'brown'", tokens.get(2).getText().equals("brown"));
        assertTrue("missing token 'fox'", tokens.get(3).getText().equals("fox"));
    }
    
    @Test
    public void linebreakHyphenTest() throws IOException {
        String txt = "We, therefore, the represen-\ntatives of the United States";
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.LINEBREAK);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<Token> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==8);
        assertTrue("missing token 'representatives'", tokens.get(3).getText().equals("representatives"));
        
        tokens.clear();
        txt = "We, therefore, the represen- \ntatives of the United States";
        tokenizer.tokenize( new StringReader(txt));
        tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==8);
        assertTrue("missing token 'representatives'", tokens.get(3).getText().equals("representatives"));
    }
}
