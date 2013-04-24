package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.juxtasoftware.XmlUtils.XmlType;

/**
 * Unit test for for the NamespaceExtractor. Verify that it can detect the
 * supported XML flavors correctly
 * 
 * @author loufoster
 */
public class XmlUtilsTest {
    
    @Test 
    public void testTei() {
        InputStream is = getClass().getResourceAsStream("/MD_AmerCh1b.xml");
        InputStreamReader r = new InputStreamReader(is);
        XmlType type = XmlUtils.determineXmlType(r);
        IOUtils.closeQuietly(r);
        assertTrue("Unable to detect TEI", type.equals(XmlType.TEI));
    }
    
    @Test 
    public void testRam() {
        InputStream is = getClass().getResourceAsStream("/46p-1849.sa76.raw.xml");
        InputStreamReader r = new InputStreamReader(is);
        XmlType type = XmlUtils.determineXmlType(r);
        IOUtils.closeQuietly(r);
        assertTrue("Unable to detect RAM", type.equals(XmlType.RAM));
        
        is = getClass().getResourceAsStream("/rossetti.xml");
        r = new InputStreamReader(is);
        type = XmlUtils.determineXmlType(r);
        IOUtils.closeQuietly(r);
        assertTrue("Unable to detect RAM", type.equals(XmlType.RAM));
    }
    
    @Test 
    public void testGeneric() {
        InputStream is = getClass().getResourceAsStream("/note.xml");
        InputStreamReader r = new InputStreamReader(is);
        XmlType type = XmlUtils.determineXmlType(r);
        IOUtils.closeQuietly(r);
        assertTrue("Unable to detect generic XML", type.equals(XmlType.GENERIC));
    }
    
    @Test 
    public void testIsXml() throws ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("/roses.txt");
        InputStreamReader r = new InputStreamReader(is);
        boolean isXml = XmlUtils.isXml(r);
        IOUtils.closeQuietly(r);
        assertTrue("Incorrect XML detection", isXml==false);
        
        is = getClass().getResourceAsStream("/MD_AmerCh1b.xml");
        r = new InputStreamReader(is);
        isXml = XmlUtils.isXml(r);
        IOUtils.closeQuietly(r);
        assertTrue("Incorrect XML detection", isXml==true);
        
        is = getClass().getResourceAsStream("/bad.xml");
        r = new InputStreamReader(is);
        isXml = XmlUtils.isXml(r);
        IOUtils.closeQuietly(r);
        assertTrue("Incorrect XML detection", isXml==false);
    }
}
