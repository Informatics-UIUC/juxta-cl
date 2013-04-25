package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.juxtasoftware.model.Configuration;

/**
 * Unit test for JuxtaCLTest; high level tests that validate command line 
 * argument processing and happy day simple text compare
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
    public void testCompareSame() throws IOException {
        
        File testFile = resourceToFile("roses.txt");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.addFile(testFile.getPath() );
        
        this.juxtaCL.setConfig(config);
        int changeIdx = this.juxtaCL.doComparison();
        assertTrue("Same files have non-zero change index", changeIdx == 0);
    }
    
    @Test
    public void testCompareDifferent() throws IOException {
        // FIXME add this back in later!
//        File testFile = resourceToFile("roses.txt");
//        File testFile2 = resourceToFile("roses2.txt");
//        Configuration config = new Configuration();
//        config.addFile(testFile.getPath() );
//        config.addFile(testFile2.getPath() );
//        
//        this.juxtaCL.setConfig(config);
//        int changeIdx = this.juxtaCL.doComparison();
//        assertTrue("Different files have zero change index", changeIdx != 0);
    }
}
