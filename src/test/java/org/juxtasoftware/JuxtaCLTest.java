package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for JuxtaCLTest; high level tests that validate command line 
 * argument processing and happy day simple text compare
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/applicationContext.xml"})
public class JuxtaCLTest {
    @Autowired JuxtaCL juxtaCL;

    @Test 
    public void testMissingComparands() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {""});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted no comparands on command line", caughtException);
    }
    
    @Test 
    public void testOneComparand() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"file1"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted one comparand on command line", caughtException);
    }
    
    @Test 
    public void testTooManyComparands() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"file1", "file2", "file3"});
        } catch (Exception e ) {
            caughtException = true;
        }
        assertTrue("Accepted too many comparands on command line", caughtException);
    }
    
    @Test 
    public void testInvalidPath() {
        boolean caughtException = false;
        try {
            this.juxtaCL.parseArgs(new String[] {"/tmp/invalid/file.txt", "/tmp/invalid/file.txt"});
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
        
        File testFile = resourceToFile("roses.txt");
        File testFile2 = resourceToFile("roses2.txt");
        Configuration config = new Configuration();
        config.addFile(testFile.getPath() );
        config.addFile(testFile2.getPath() );
        
        System.out.println("test");
        this.juxtaCL.setConfig(config);
        int changeIdx = this.juxtaCL.doComparison();
        assertTrue("Different files have zero change index", changeIdx != 0);
    }
   
    private File resourceToFile(String resourceName) throws IOException {
        InputStream is = getClass().getResourceAsStream("/"+resourceName);
        File local = File.createTempFile("resource", "dat");
        FileOutputStream fos = new FileOutputStream(local);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(fos);
        local.deleteOnExit();
        return local;
    }
}
