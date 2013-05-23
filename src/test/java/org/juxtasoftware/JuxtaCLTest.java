package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.juxtasoftware.model.Configuration;
import org.juxtasoftware.model.Configuration.Algorithm;
import org.juxtasoftware.model.Configuration.Mode;
import org.juxtasoftware.model.DiffException;
import org.juxtasoftware.model.EncodingException;
import org.juxtasoftware.model.TagStripException;

/**
 * Unit test for JuxtaCLTest; high level tests that validate happy day simple text compare
 * and tag extraction
 */
public class JuxtaCLTest extends JuxtaBase {
    
    @Test 
    public void testInvalidPath() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"-diff", "/tmp/invalid/file.txt", "/tmp/invalid/file.txt"});
            this.juxtaCL.execute();
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted invalid file paths", caughtException);
    }
    
    @Test
    public void testExtract() throws DiffException, TagStripException, IOException, EncodingException {
        File testFile = resourceToFile("note.xml");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.setMode(Mode.STRIP);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        String out = this.sysOut.toString();
        assertTrue("bad text extract", out.equals("TestTest2NoteBody\n"));
    }
	 
    @Test
    public void testCompareSame() throws DiffException, FileNotFoundException, IOException, EncodingException, TagStripException {
        File testFile = resourceToFile("roses.txt");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.addFile(testFile.getPath() );
        config.setAlgorithm(Algorithm.JUXTA);
        config.setMode(Mode.DIFF);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        String out = this.sysOut.toString();
        assertTrue("Same files have < 1 change index", Float.parseFloat(out) == 1);
    }
    
    @Test
    public void testCompareDifferent() throws IOException, EncodingException, DiffException, TagStripException {
        File testFile = resourceToFile("roses.txt");
        File testFile2 = resourceToFile("roses2.txt");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.addFile(testFile2.getPath() );
        config.setMode(Mode.DIFF);
        config.setAlgorithm(Algorithm.JUXTA);
        this.juxtaCL.setConfig(config);
        this.juxtaCL.execute();
        String out = this.sysOut.toString();
        assertTrue("Different files have 1 change index", Float.parseFloat(out) != 1);
    }
}
