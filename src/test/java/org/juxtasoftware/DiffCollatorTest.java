package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Configuration.Algorithm;
import org.juxtasoftware.model.Configuration.Hyphens;
import org.juxtasoftware.model.Configuration.Mode;
import org.juxtasoftware.model.DiffException;
import org.juxtasoftware.model.EncodingException;
import org.juxtasoftware.model.TagStripException;

public class DiffCollatorTest extends JuxtaBase {
    private Tokenizer tokenizer;
    private Configuration config;
    
    @Before
    public void setup() {
        super.setup();
        this.tokenizer = new Tokenizer();
        this.config= new Configuration();
        this.config.setMode(Mode.DIFF);
        this.config.setHyphenation(Hyphens.ALL);
        this.config.setIgnoreCase(true);
        this.config.setIgnorePunctuation(true);
        this.tokenizer.setConfig(this.config);
    }
    
    @Test
    public void rosesTest() throws IOException {
        File testFileA = resourceToFile("roses.txt");
        File testFileB = resourceToFile("roses2.txt");
        List<String> a = this.tokenizer.tokenize(new FileReader(testFileA));
        long lenA = this.tokenizer.getTokenizedLength();
        List<String> b = this.tokenizer.tokenize(new FileReader(testFileB));
        long lenB = this.tokenizer.getTokenizedLength();
        
        DiffCollator diff = new DiffCollator();
        
        diff.setAlgorithm(Algorithm.JUXTA);
        float ci = diff.diff(a, b, lenA, lenB);
        System.out.println("Juxta: "+ci);
        
        diff.setAlgorithm(Algorithm.LEVENSHTEIN);
        ci = diff.diff(a, b, lenA, lenB);
        System.out.println("lev: "+ci);
        
        diff.setAlgorithm(Algorithm.JARO_WINKLER);
        ci = diff.diff(a, b, lenA, lenB);
        System.out.println("jw: "+ci);
    }
    
    @Test
    public void basicJaroWinklerTest() throws IOException {
        String txtA = "the quick brown fox";
        String txtB = "the quick vrown fox";
        List<String> a = this.tokenizer.tokenize(new StringReader(txtA));
        long lenA = this.tokenizer.getTokenizedLength();
        List<String> b = this.tokenizer.tokenize(new StringReader(txtB));
        long lenB = this.tokenizer.getTokenizedLength();
        
        DiffCollator diff = new DiffCollator();
        diff.setAlgorithm(Algorithm.JARO_WINKLER);
        float ci = diff.diff(a, b, lenA, lenB);
        
        // in jaro winkler tokens that are the same get a value of 1.
        // differences are < 1. using this algoithm, the value of
        // the change from brown -> vrown gets a value of: 0.866.
        // algotithm computes average jw value over all tokens
        // for this it would be ( 1 + 1 + 0.866 + 1 ) / 4 = .9666
        // invert this to make 0 same and one different: gives 0.0333
        assertTrue("bad jaro-winkler result", ci==(1f-0.0333333f) );
    }
    
    @Test
    public void basicLevenshteinTest() throws IOException {
        String txtA = "the quick brown fox";
        String txtB = "the quick vrown fox";
        List<String> a = this.tokenizer.tokenize(new StringReader(txtA));
        long lenA = this.tokenizer.getTokenizedLength();
        List<String> b = this.tokenizer.tokenize(new StringReader(txtB));
        long lenB = this.tokenizer.getTokenizedLength();
        
        DiffCollator diff = new DiffCollator();
        diff.setAlgorithm(Algorithm.LEVENSHTEIN);
        float ci = diff.diff(a, b, lenA, lenB);
        
        // 16 letters considered in the diff. only 1 has been changed.
        // this makes the levenhtein diff result in:
        // total levenshtein distance / total letters compared
        // 1 / 16 =  0.0625
        assertTrue("incorrect levenshtein distance diff result", ci == (1f-0.0625));
    }
    
    @Test
    public void basicJuxtaTest() throws IOException {
        String txtA = "the quick brown fox";
        String txtB = "the quick vrown fox";
        List<String> a = this.tokenizer.tokenize(new StringReader(txtA));
        long lenA = this.tokenizer.getTokenizedLength();
        List<String> b = this.tokenizer.tokenize(new StringReader(txtB));
        long lenB = this.tokenizer.getTokenizedLength();
        
        DiffCollator diff = new DiffCollator();
        diff.setAlgorithm(Algorithm.JUXTA);
        float ci = diff.diff(a, b, lenA, lenB);
        
        // 16 letters considered in the diff. one 5 letter word has changed
        // this makes the juxta diff result in:
        // total word letters in diff / max witness length
        // 5 / 16 =  0.3125
        assertTrue("incorrect juxta distance diff result", ci == (1f-0.3125));
    }
    
    @Test
    public void realWorldJuxtaTest() throws IOException, DiffException, TagStripException, EncodingException {
        
        
        File testFileA = resourceToFile("MD_AmerCh1b.xml");
        File testFileB = resourceToFile("MD_Brit_v1CH1a.xml");
        Configuration cfg = new Configuration();
        cfg.addFile(testFileA.getPath() );
        cfg.addFile(testFileB.getPath() );
        cfg.setMode(Mode.DIFF);
        cfg.setAlgorithm(Algorithm.JUXTA);
        cfg.setHyphenation(Hyphens.LINEBREAK);
        this.juxtaCL.setConfig(cfg);
        this.juxtaCL.execute();
        
        String out = this.sysOut.toString();
        float val = Float.parseFloat(out);
        String foo = String.format("%1.2f", val);
        
        // the value from juxtaWS is 6%
        float expected = 1f-Float.parseFloat(foo);
        foo = String.format("%1.2f", expected);
        assertTrue("incorrect juxta distance diff result", foo.equals("0.06"));
    }
}
