package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for XML tag stripper
 */
public class XmlTagStripperTest {
    JuxtaCL juxtaCL = new JuxtaCL();

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
}
