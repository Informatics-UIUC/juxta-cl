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

public class DiffCollatorTest extends JuxtaBase {
    private Tokenizer tokenizer;
    private Configuration config;
    
//    @Before
//    public void setup() {
//        this.tokenizer = new Tokenizer();
//        this.config= new Configuration();
//        this.config.setMode(Mode.DIFF);
//        this.config.setHyphenation(Hyphens.ALL);
//        this.config.setIgnoreCase(true);
//        this.config.setIgnorePunctuation(true);
//        this.tokenizer.setConfig(this.config);
//    }
//    
//    @Test
//    public void rosesTest() throws IOException {
//        File testFileA = resourceToFile("roses.txt");
//        File testFileB = resourceToFile("roses2.txt");
//        List<String> a = this.tokenizer.tokenize(new FileReader(testFileA));
//        List<String> b = this.tokenizer.tokenize(new FileReader(testFileB));
//        
//        DiffCollator diff = new DiffCollator();
//        
//        diff.setAlgorithm(Algorithm.JUXTA);
//        float ci = diff.diff(a, b);
//        System.out.println("Juxta: "+ci);
//        
//        diff.setAlgorithm(Algorithm.LEVENSHTEIN);
//        ci = diff.diff(a, b);
//        System.out.println("lev: "+ci);
//        
//        diff.setAlgorithm(Algorithm.JARO_WINKLER);
//        ci = diff.diff(a, b);
//        System.out.println("jw: "+ci);
//    }
//    
//    @Test
//    public void basicJaroWinklerTest() throws IOException {
//        String txtA = "the quick brown fox";
//        String txtB = "the quick vrown fox";
//        List<String> a = this.tokenizer.tokenize(new StringReader(txtA));
//        List<String> b = this.tokenizer.tokenize(new StringReader(txtB));
//        
//        DiffCollator diff = new DiffCollator();
//        diff.setAlgorithm(Algorithm.JARO_WINKLER);
//        float ci = diff.diff(a, b);
//        System.err.println(ci);
//    }
//    
//    @Test
//    public void basicLevenshteinTest() throws IOException {
//        String txtA = "the quick brown fox";
//        String txtB = "the quick vrown fox";
//        List<String> a = this.tokenizer.tokenize(new StringReader(txtA));
//        List<String> b = this.tokenizer.tokenize(new StringReader(txtB));
//        
//        DiffCollator diff = new DiffCollator();
//        diff.setAlgorithm(Algorithm.LEVENSHTEIN);
//        float ci = diff.diff(a, b);
//        
//        // 16 letters considered in the diff. only 1 has been changed.
//        // this makes the levenhtein diff result in:
//        // total levenshtein distance / total letters compared
//        // 1 / 16 =  0.0625
//        assertTrue("incorrect levenshtein distance diff result", ci == 0.0625);
//    }
//    
//    @Test
//    public void basicJuxtaTest() throws IOException {
//        String txtA = "the quick brown fox";
//        String txtB = "the quick vrown fox";
//        List<String> a = this.tokenizer.tokenize(new StringReader(txtA));
//        List<String> b = this.tokenizer.tokenize(new StringReader(txtB));
//        
//        DiffCollator diff = new DiffCollator();
//        diff.setAlgorithm(Algorithm.JUXTA);
//        float ci = diff.diff(a, b);
//        
//        // 16 letters considered in the diff. only 1 has been changed.
//        // this makes the juxta diff result in:
//        // total letters diff / max witness length
//        // 1 / 16 =  0.0625
//        assertTrue("incorrect levenshtein distance diff result", ci == 0.0625);
//    }
}
