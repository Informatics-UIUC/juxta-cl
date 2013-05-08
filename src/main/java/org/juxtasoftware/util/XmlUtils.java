package org.juxtasoftware.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Extract all namespace info from an XML source. Flavors of XML detected are:
 *   TEI, RAM and generic XML
 *   
 * @author loufoster
 *
 */
public final class XmlUtils {
    public enum XmlType {GENERIC, TEI, RAM, GALE};
    private XmlUtils() {
        throw new RuntimeException("Can't instantiate XmlUtils");
    }
    
    /**
     * Return true if this file is valid xml
     * @param fileReader
     * @return
     */
    public static boolean isValidXml( final Reader fileReader ) { 
        return isValidXml(fileReader, false);
    }
    public static boolean isValidXml( final Reader fileReader, boolean logErrors ) {
        DocumentBuilder builder = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            builder = factory.newDocumentBuilder();
            
            // ignore dtd and stuff during validation
            builder.setEntityResolver(new EntityResolver() {

                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.endsWith(".dtd") || systemId.endsWith(".ent")) {
                        StringReader stringInput = new StringReader(" ");
                        return new InputSource(stringInput);
                    }
                    else {
                        return null; // use default behavior
                    }
                }
            });
        } catch (ParserConfigurationException e1) {
            return false;
        }
        
        try {
            if ( logErrors ) {
                builder.setErrorHandler( new LoggingErrorHandler() );
            } else {
                builder.setErrorHandler( null );
            }
            builder.parse( new InputSource(fileReader));
            return true;
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    private static class LoggingErrorHandler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            System.out.println("Warning ");
            printInfo(e);
        }

        public void error(SAXParseException e) throws SAXException {
            System.out.println("Error ");
            printInfo(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            System.out.println("Fatal Error ");
            printInfo(e);
        }

        private void printInfo(SAXParseException e) {
            System.out.println("  Line number: " + e.getLineNumber());
            System.out.println("  Column number: " + e.getColumnNumber());
            System.out.println("  "+e.getMessage());
        }
    }
    
    /**
     * Examine the namespace declarations of this source and attempt to determine
     * the XML type: TEI, RAM or Generic
     * 
     * @param srcReader
     * @return
     */
    public static XmlType determineXmlType(final Reader srcReader) {
        BufferedReader  br = new BufferedReader(srcReader);
        int lineCnt = 0;
        XmlType type = XmlType.GENERIC;
        try {
            while ( true ) {
                String line = br.readLine();
                if ( line == null ) {
                    break;
                } else {
                    if ( line.contains("http://www.tei-") || line.contains("tei2.dtd") || 
                         line.contains("teiCorpus") || line.contains("DOCTYPE TEI") || line.contains("<TEI")) {
                        type = XmlType.TEI;
                        break;
                    } else if ( line.contains("ram.xsd")) {
                        type = XmlType.RAM;
                        break;
                    } else if ( line.contains("book.dtd") || line.contains("<bookInfo>")) {
                        type = XmlType.GALE;
                        break;
                    }

                    // if we haven't found it  in 20 lines.. give up
                    lineCnt++;
                    if (lineCnt > 20 ) {
                        break;
                    }
                } 
            }
        } catch (IOException e ) {
            // swallow it
        } finally {
            IOUtils.closeQuietly(br);
        }
        return type;
    }
    
    public static boolean hasNamespace( final String xmlContent) {
        final String defaultNs = "xmlns=\"";
        final String noNamespace = ":noNamespaceSchemaLocation=\"";
        final String ns = "xmlns:";
        if ( xmlContent.contains(noNamespace)) {
            return false;
        }
        return ( xmlContent.contains(defaultNs) || xmlContent.contains(ns));
    }
}
