package org.juxtasoftware;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.juxtasoftware.XmlUtils.XmlType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Strip XML tags and return plain text content
 * 
 * @author loufoster
 *
 */
public class XmlTagStripper {
    private static Logger LOG = Logger.getLogger(XmlTagStripper.class);
    
    public String stripTags(File xmlFile) throws IOException, SAXException, TransformerException {
        LOG.info("Ensure file is XML");
        FileReader r = new FileReader(xmlFile);
        String content = IOUtils.toString(r);
        IOUtils.closeQuietly(r);

        boolean isXml = XmlUtils.isXml( new StringReader(content) );
        
        if ( isXml == false ) {
            throw new RuntimeException("Not an XML file");
        }
        
        LOG.info("Determine XML flavor");
        XmlType xmlType = XmlUtils.determineXmlType( new StringReader(content) );
        IOUtils.closeQuietly(r);
        
        if ( xmlType.equals(XmlType.TEI)) {
            
            return extractTeiText(content);
        } else if ( xmlType.equals(XmlType.RAM)) {
            
            return extractRamText(content);
        } else {
            
            return extractXmlText(content);
        }
    }

    private String extractTeiText(String content) throws IOException, SAXException, TransformerException {
        LOG.info("Extract TEI text");
        String teiXslt = "";
        if ( XmlUtils.hasNamespace(content)) {
            teiXslt = IOUtils.toString( ClassLoader.getSystemResourceAsStream("xslt/tei.xslt"), "utf-8");
        } else {
            teiXslt = IOUtils.toString( ClassLoader.getSystemResourceAsStream("xslt/tei-nons.xslt"), "utf-8");
        }
        return doExtract(content, teiXslt);
    }

    private String doExtract(String content, String teiXslt) throws SAXException, TransformerFactoryConfigurationError,
        TransformerConfigurationException, TransformerException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setEntityResolver(new EntityResolver() {

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
        SAXSource xmlSource = new SAXSource(reader, new InputSource( new StringReader(content) ));

        
        javax.xml.transform.Source xsltSource =  new StreamSource( new StringReader(teiXslt) );
        StreamResult xmlOutput = new StreamResult(new StringWriter());
 
        // create an instance of TransformerFactory and do the transform
        TransformerFactory factory = TransformerFactory.newInstance(  );
        Transformer transformer = factory.newTransformer(xsltSource);  
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text");
        transformer.transform(xmlSource, xmlOutput);
        
        return xmlOutput.getWriter().toString();
    }
    
    private String extractRamText(String content) throws IOException, SAXException, TransformerException {
        LOG.info("Extract RAM text");        
        String teiXslt = IOUtils.toString( ClassLoader.getSystemResourceAsStream("xslt/ram.xslt"), "utf-8");
        return doExtract(content, teiXslt);
    }
    
    private String extractXmlText(String content) throws IOException, SAXException, TransformerException {
        LOG.info("Extract generic XML text");        
        String teiXslt = IOUtils.toString( ClassLoader.getSystemResourceAsStream("xslt/general.xslt"), "utf-8");
        return doExtract(content, teiXslt);
    }

}
