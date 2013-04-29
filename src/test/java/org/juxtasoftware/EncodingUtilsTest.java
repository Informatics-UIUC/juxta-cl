package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.juxtasoftware.util.EncodingUtils;

public class EncodingUtilsTest extends JuxtaBase {
    
    @Test
    public void testDetectUtf8() throws IOException {
        File f = resourceToFile("rossetti.xml");
        try {
            String out = EncodingUtils.detectEncoding(f);
            assertTrue("incorrect encoding detected", out.equalsIgnoreCase("UTF-8"));
        } catch (IOException e) {
            assertTrue("unable to detect encoding", false);
        }
    }
    
    @Test
    public void testDetectNonUtf8() throws IOException {
        File f = resourceToFile("note_utf-16le.xml");
        try {
            String out = EncodingUtils.detectEncoding(f);
            assertTrue("incorrect encoding detected", out.equalsIgnoreCase("UTF-16LE"));
        } catch (IOException e) {
            assertTrue("unable to detect encoding", false);
        }
    }
    
    @Test
    public void testFixNonUtf8() throws IOException {
        InputStream is = getClass().getResourceAsStream("/note_encode_1252_u.xml");
        try {
            File out = EncodingUtils.fixEncoding(is);
            
            FileInputStream fis = new FileInputStream(out);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            String dat = IOUtils.toString(isr);
            IOUtils.closeQuietly(isr);
            assertTrue("did not strip encoding", dat.contains("windows-1252") == false);
            assertTrue("text is broken", dat.indexOf("æøå") > -1);
            out.delete();

        } catch (IOException e) {
            assertTrue("unable to detect encoding", false);
        }
    }
    
    @Test
    public void testStripBadUtf8() throws IOException {
        File f = resourceToFile("alice_under_ground.txt");
        try {
            File fixed = EncodingUtils.stripUnknownUTF8(f);
            FileReader fr = new FileReader(fixed);
            String dat = IOUtils.toString(fr);
            fr.close();
            fixed.delete();
            char bad = 0xfffd;
            String badStr = ""+bad;
            assertTrue("did not strip bad chars", dat.contains(badStr) == false);
        } catch (IOException e) {
            assertTrue("unable to detect encoding", false);
        }
    }
}
