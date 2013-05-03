package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Configuration.Mode;
import org.juxtasoftware.model.DiffException;
import org.juxtasoftware.model.EncodingException;
import org.juxtasoftware.model.TagStripException;

/**
 * Unit test for XML tag stripper
 */
public class XmlTagStripperTest extends JuxtaBase {
    
    @Test 
    public void testInvalidPath() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-strip", "/tmp/invalid/file.xml"});
            this.juxtaCL.execute();
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted invalid file paths", caughtException);
    }
    
    @Test 
    public void testStripTxtFile() {
        boolean caughtException = false;
        try {
            File testFile = resourceToFile("roses.txt");
            Configuration config = new Configuration();
            config.addFile(testFile.getPath() );
            config.setMode(Mode.STRIP);
            this.juxtaCL.setConfig(config);
            this.juxtaCL.execute();
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted TXT file", caughtException);
    }
    
    @Test 
    public void testStripMalformedFile() {
        boolean caughtException = false;
        try {
            File testFile = resourceToFile("bad.xml");
            Configuration config = new Configuration();
            config.addFile(testFile.getPath() );
            config.setMode(Mode.STRIP);
            this.juxtaCL.setConfig(config);
            this.juxtaCL.execute();
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted malformed XML file", caughtException);
    }
    
    @Test 
    public void testStripGenericXml() throws IOException, TagStripException, EncodingException, DiffException {
        File testFile = resourceToFile("note.xml");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.setMode(Mode.STRIP);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        
        String out = this.sysOut.toString();
        assertTrue("No text extracted", out.length() > 0);
        assertTrue("Incorrect text extracted", out.equals("TestTest2NoteBody\n"));
    }
    
    @Test 
    public void testStripTeiXml() throws IOException, TagStripException, EncodingException, DiffException {
        File testFile = resourceToFile("MD_AmerCh1b.xml");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.setMode(Mode.STRIP);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        
        String out = this.sysOut.toString();
        assertTrue("No text extracted", out.length() > 0);
        assertTrue("Incorrect text extracted", out.contains("CHAPTER I.\nloomings.\nCall me Ishmael."));
    }
    
    @Test 
    public void testStripRamXml() throws IOException, TagStripException, EncodingException, DiffException {
        File testFile = resourceToFile("ram.xml");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.setMode(Mode.STRIP);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        
        String out = this.sysOut.toString();
        assertTrue("No text extracted", out.length() > 0);
        assertTrue("Incorrect text extracted", out.contains("=======\nThe Cry of the P.R.B."));
    }
    
    @Test 
    public void testStripGaleXml() throws IOException, TagStripException, EncodingException, DiffException {
        File testFile = resourceToFile("galesample.xml");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.setMode(Mode.STRIP);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        
        String out = this.sysOut.toString().replaceAll("\n", " ");
        assertTrue("No text extracted", out.length() > 0);
        assertTrue("Incorrect text extracted", out.contains("a Proposal shall not be wanting. FINIS."));
    }

}
