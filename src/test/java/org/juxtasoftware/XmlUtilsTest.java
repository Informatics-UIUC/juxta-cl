package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.juxtasoftware.util.XmlUtils;
import org.juxtasoftware.util.XmlUtils.XmlType;

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
    public void testTeiNamespaceDetection() throws IOException {
        InputStream is = getClass().getResourceAsStream("/Bijou1828.xml");
        InputStreamReader r = new InputStreamReader(is);
        String xml = IOUtils.toString(r);
        boolean hasNs = XmlUtils.hasNamespace(xml);
        IOUtils.closeQuietly(r);
        assertTrue("Unable to detect no namespace TEI", (hasNs == false));
        
        is = getClass().getResourceAsStream("/MD_AmerCh1b.xml");
        r = new InputStreamReader(is);
        xml = IOUtils.toString(r);
        hasNs = XmlUtils.hasNamespace(xml);
        IOUtils.closeQuietly(r);
        assertTrue("Unable to detect namespace TEI", (hasNs == true));
    }
    
    @Test 
    public void testRam() {
        InputStream is = getClass().getResourceAsStream("/ram.xml");
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
    public void testIsValidXml() throws ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("/roses.txt");
        InputStreamReader r = new InputStreamReader(is);
        boolean isXml = XmlUtils.isValidXml(r);
        IOUtils.closeQuietly(r);
        assertTrue("Incorrect XML detection", isXml==false);
        
        is = getClass().getResourceAsStream("/MD_AmerCh1b.xml");
        r = new InputStreamReader(is);
        isXml = XmlUtils.isValidXml(r);
        IOUtils.closeQuietly(r);
        assertTrue("Incorrect XML detection", isXml==true);
        
        is = getClass().getResourceAsStream("/bad.xml");
        r = new InputStreamReader(is);
        isXml = XmlUtils.isValidXml(r);
        IOUtils.closeQuietly(r);
        assertTrue("Incorrect XML detection", isXml==false);
    }
}
