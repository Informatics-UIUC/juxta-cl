package org.juxtasoftware;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli2.OptionException;
import org.junit.Test;
import org.juxtasoftware.Configuration.Hyphens;

public class CommandLineParserTest {
    JuxtaCL juxtaCL = new JuxtaCL();
    
    @Test 
    public void testMissingDiffComparands() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-diff"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted no comparands on command line", caughtException);
    }
    
    @Test 
    public void testOneDiffComparand() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-diff", "file1"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted one comparand on command line", caughtException);
    }
    
    @Test 
    public void testTooManyDiffComparands() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "file3"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted too many comparands on command line", caughtException);
    }
    
    @Test 
    public void testCaseSwitch() throws OptionException {
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "+caps"});
        assertFalse("Shouldn't be ignoring caps", this.juxtaCL.getConfig().isIgnoreCase());
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "-caps"});
        assertTrue("Should be ignoring caps", this.juxtaCL.getConfig().isIgnoreCase());
    }
    
    @Test 
    public void testPunctuationSwitch() throws OptionException {
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "+punct"});
        assertFalse("Shouldn't be ignoring punctuation", this.juxtaCL.getConfig().isIgnorePunctuation());
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "-punct"});
        assertTrue("Should be ignoring punctuation", this.juxtaCL.getConfig().isIgnorePunctuation());
    }
    
    @Test 
    public void testBadHyphenationSettings() throws OptionException {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "-hyphen", "ferret"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted invalid hyphen setting on command line", caughtException);
    }
    
    @Test 
    public void testHyphenationSettings() throws OptionException {
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "-hyphen", "all"});
        assertTrue("Hyphen setting should be ALL", this.juxtaCL.getConfig().getHyphenation().equals(Hyphens.ALL));
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "-hyphen", "linebreak"});
        assertTrue("Hyphen setting should be LINEBREAK", this.juxtaCL.getConfig().getHyphenation().equals(Hyphens.LINEBREAK));
        this.juxtaCL.parseArgs(new String[] {"-diff", "file1", "file2", "-hyphen", "none"});
        assertTrue("Hyphen setting should be NONE", this.juxtaCL.getConfig().getHyphenation().equals(Hyphens.NONE));
    }

    @Test 
    public void testTooFewXmlFiles() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-strip"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted missing XML file for tag strip", caughtException);
    }
    
    @Test 
    public void testTooManyXmlFiles() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-strip", "test1.xml", "test2.xml"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted too many XML files for tag strip", caughtException);
    }
    
    @Test 
    public void testValidStripParams() {
        try {
            this.juxtaCL.parseArgs(new String[] {"-strip", "test1.xml", "--verbose"});
        } catch (Exception e ) {
            assertTrue("Threw exception on valid cmd line args", true);
        }
    }
}
