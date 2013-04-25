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
        txt = "We, therefore, the represen - \ntatives of the United States";
        tokenizer.tokenize( new StringReader(txt));
        tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==8);
        assertTrue("missing token 'representatives'", tokens.get(3).getText().equals("representatives"));
    }
    
    @Test
    public void ignoreLinebreakTest() throws IOException {
        String txt = "week-days";
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.NONE);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<Token> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==2);
        assertTrue("missing token 'week'", tokens.get(0).getText().equals("week"));
        assertTrue("missing token 'days'", tokens.get(1).getText().equals("days"));
    }
    
    @Test
    public void punctuationTest() throws IOException {
        String txt = "this... is a big, scary test!";
        String[] expected1 = {"this", "is", "a", "big", "scary", "test"};
        String[] expected2 = {"this", "...", "is", "a", "big", ",", "scary", "test", "!"};
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.ALL);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<Token> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==expected1.length);
        for ( int i=0; i<expected1.length; i++ ) {
            boolean match = false;
            for ( Token t : tokens ) {
                if (t.getText().equals(expected1[i])) {
                    match = true;
                }
            }
            assertTrue("Missing expected token when ignoring punctuation",match);
        }
        
        cfg.setIgnorePunctuation(false);
        tokenizer.tokenize( new StringReader(txt));
        tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==expected2.length);
        for ( int i=0; i<expected2.length; i++ ) {
            boolean match = false;
            for ( Token t : tokens ) {
                if (t.getText().equals(expected2[i])) {
                    match = true;
                }
            }
            assertTrue("Missing expected token when NOT ignoring punctuation",match);
        }
    }
    
    @Test
    public void caseTest() throws IOException {
        String txt = "Black CAT";
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.ALL);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<Token> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==2);
        assertTrue("missing token 'black'", tokens.get(0).getText().equals("black"));
        assertTrue("missing token 'cat'", tokens.get(1).getText().equals("cat"));
        
        cfg.setIgnoreCase(false);
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==2);
        assertTrue("missing token 'black'", tokens.get(0).getText().equals("Black"));
        assertTrue("missing token 'cat'", tokens.get(1).getText().equals("CAT"));
    }
}
