package org.juxtasoftware;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for JuxtaCLTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/applicationContext.xml"})
public class JuxtaCLTest {

    @Test
    public void testCompareSame() throws IOException {
        
        File testFile = resourceToFile("roses.txt");
        
        JuxtaCL juxtaCl = new JuxtaCL();
        int changeIdx = juxtaCl.compare(testFile.getPath(), testFile.getPath());
        assertTrue(changeIdx == 0);
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
