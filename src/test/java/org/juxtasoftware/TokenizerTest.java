package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Configuration.Hyphens;

/**
 * Tests for the tokenizer
 * 
 * @author loufoster
 *
 */
public class TokenizerTest extends JuxtaBase {

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
        List<String> tokens = tokenizer.getTokens();
        
        assertTrue("Wrong number of tokens", tokens.size()==4);
        assertTrue("missing token 'the'", tokens.get(0).equals("the"));
        assertTrue("missing token 'quick'", tokens.get(1).equals("quick"));
        assertTrue("missing token 'brown'", tokens.get(2).equals("brown"));
        assertTrue("missing token 'fox'", tokens.get(3).equals("fox"));
    }
    
    @Test
    public void complexHyphenIgnoreTest() throws IOException {
        InputStream is = getClass().getResourceAsStream("/hyphroses.txt");
        String txt = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.NONE);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<String> tokens = tokenizer.getTokens();
        assertTrue("wrong number of tokens", tokens.size() == 12);
        assertTrue("content bad", tokens.get(0).equals("roses"));
        assertTrue("content bad", tokens.contains("blueskunk"));
        assertTrue("content bad", tokens.get(11).equals("you"));
    }
    
    @Test
    public void complexHyphenLinebreakTest() throws IOException {
        InputStream is = getClass().getResourceAsStream("/hyphroses.txt");
        String txt = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.LINEBREAK);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<String> tokens = tokenizer.getTokens();
        assertTrue("wrong number of tokens", tokens.size() == 18);
        assertTrue("content bad", tokens.get(0).equals("ro"));
        assertTrue("includes hyphen", tokens.contains("-") == false);
        assertTrue("missing joined linebreak word", tokens.contains("blueskunk"));
        assertTrue("content bad", tokens.get(17).equals("you"));
    }
    
    @Test
    public void complexHyphenAllTest() throws IOException {
        InputStream is = getClass().getResourceAsStream("/hyphroses.txt");
        String txt = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        
        Configuration cfg = new Configuration();
        cfg.setIgnoreCase(true);
        cfg.setIgnorePunctuation(true);
        cfg.setHyphenation(Hyphens.ALL);
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        List<String> tokens = tokenizer.getTokens();
        assertTrue("wrong number of tokens", tokens.size() == 19);
        assertTrue("includes hyphen", tokens.contains("-") == false);
        assertTrue("missing joined linebreak word", tokens.contains("blueskunk")==false);
        assertTrue("content bad", tokens.get(18).equals("you"));
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
        List<String> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==8);
        assertTrue("missing token 'representatives'", tokens.get(3).equals("representatives"));
        
        tokens.clear();
        txt = "We, therefore, the represen - \ntatives of the United States";
        tokenizer.tokenize( new StringReader(txt));
        tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==8);
        assertTrue("missing token 'representatives'", tokens.get(3).equals("representatives"));
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
        List<String> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==1);
        assertTrue("missing token 'weekdays'", tokens.get(0).equals("weekdays"));
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
        List<String> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==expected1.length);
        for ( int i=0; i<expected1.length; i++ ) {
            boolean match = false;
            for ( String t : tokens ) {
                if (t.equals(expected1[i])) {
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
            for ( String t : tokens ) {
                if (t.equals(expected2[i])) {
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
        List<String> tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==2);
        assertTrue("missing token 'black'", tokens.get(0).equals("black"));
        assertTrue("missing token 'cat'", tokens.get(1).equals("cat"));
        
        cfg.setIgnoreCase(false);
        tokenizer.setConfig(cfg);
        tokenizer.tokenize( new StringReader(txt));
        tokens = tokenizer.getTokens();
        assertTrue("Wrong number of tokens", tokens.size()==2);
        assertTrue("missing token 'black'", tokens.get(0).equals("Black"));
        assertTrue("missing token 'cat'", tokens.get(1).equals("CAT"));
    }
}
