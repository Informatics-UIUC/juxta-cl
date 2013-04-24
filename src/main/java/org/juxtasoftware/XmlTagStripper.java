package org.juxtasoftware;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.juxtasoftware.XmlUtils.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
@Component
public class XmlTagStripper {
    private static Logger LOG = LoggerFactory.getLogger(XmlTagStripper.class);
    
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
        String teiXslt = IOUtils.toString( ClassLoader.getSystemResourceAsStream("xslt/tei.xslt"), "utf-8");
        
        
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
    
    private String extractRamText(String content) {
        LOG.info("Extract RAM text");        
        // TODO Auto-generated method stub
        return null;
    }
    
    private String extractXmlText(String content) {
        LOG.info("Extract generic XML text");        
        // TODO Auto-generated method stub
        return null;
    }

}
