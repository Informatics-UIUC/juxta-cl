package org.juxtasoftware.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.juxtasoftware.JuxtaCL;
import org.mozilla.universalchardet.UniversalDetector;

public final class EncodingUtils {

    private static final Logger LOG = Logger.getLogger( JuxtaCL.class );
    private EncodingUtils() {
        throw new RuntimeException("Can't instantiate EncodingUtils");
    }
    
    public static final File stripUnknownUTF8(File srcFile) throws IOException {
        File fixed = null;
        BufferedReader r = null;
        OutputStreamWriter osw = null;
        try {
            fixed = File.createTempFile("txt", "dat");
            osw = new OutputStreamWriter(new FileOutputStream(fixed), "UTF-8");
            FileInputStream fis = new FileInputStream(srcFile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            r = new BufferedReader(isr);
            while (true) {
                String line = r.readLine();
                if (line == null) {
                    break;
                } else {
                    char bad = 0xfffd;
                    line = line.replaceAll("" + bad, " ");
                    osw.write(line + "\n");
                }
            }
            return fixed;
        } finally {
            IOUtils.closeQuietly(r);
            IOUtils.closeQuietly(osw);
        }
    }
    
    /**
     * Normalize the content to UTF-8 and strip any tags that say otherwise
     * @return A file containing the UTF-8 contents
     * @throws IOException 
     */
    public static File fixEncoding( InputStream source ) throws IOException {
        File tmpSrc = File.createTempFile("src", "dat");
        FileOutputStream fos =  new FileOutputStream(tmpSrc);
        IOUtils.copyLarge(source, fos);
        IOUtils.closeQuietly(fos);
 
        String encoding = EncodingUtils.detectEncoding(tmpSrc);
        if ( encoding.equalsIgnoreCase("UTF-8") ) {
            EncodingUtils.finalFixes(tmpSrc);
            return tmpSrc;
        }
            
        LOG.info("Converting from "+encoding+" to UTF-8");
        
        // stream the input in original encoding to output in UTF-8
        File utf8Out = File.createTempFile("utf8out","dat");
        Reader in = null;
        if ( encoding == "UNK" ) {
            in  = new InputStreamReader(new FileInputStream(tmpSrc), "UTF-8" ); // default to a UTF-8
        } else {
            in  = new InputStreamReader(new FileInputStream(tmpSrc), encoding);
        }
        Writer out = new OutputStreamWriter(new FileOutputStream(utf8Out), "UTF-8");
        int c;
        while ((c = in.read()) != -1){
            out.write(c);
        }
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        if (!tmpSrc.delete() ) {
            tmpSrc.deleteOnExit();
        }
        
        // lastly, strip the xml declaration and repair ^M linefeeds.
        EncodingUtils.finalFixes(utf8Out);
        return utf8Out;
    }
    
    private static void finalFixes(File tmpSrc) throws IOException {
        File out=null;
        try {
            FileInputStream fis = new FileInputStream(tmpSrc);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader r = new BufferedReader( isr );
            out = File.createTempFile("fix", "dat");
            FileOutputStream fos = new FileOutputStream(out);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            boolean pastHeader = false;
            while (true) {
                String line = r.readLine();
                if ( line != null ) {
                    if ( pastHeader == false && line.contains("<?xml") ) {
                        pastHeader = true;
                        int pos = line.indexOf("<?xml");
                        int end = line.indexOf("?>", pos);
                        line = line.substring(0,pos)+line.substring(end+2);
                    }
                    line += "\n";
                    osw.write(line);
                } else {
                    break;
                }
            }
            IOUtils.closeQuietly( r );
            IOUtils.closeQuietly(osw);

            IOUtils.copy(new FileInputStream(out), new FileOutputStream(tmpSrc));
        } finally {
            if (out != null ) {
                if (!out.delete() ) {
                    out.deleteOnExit();
                }
            }
        }
    }

    public static String detectEncoding(File srcFile) throws IOException {
        
        // feed chunks of data to the detector until it is done
        UniversalDetector detector = new UniversalDetector(null);
        byte[] buf = new byte[4096];
        int nread;
        String encoding = "utf-8";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcFile);
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            IOUtils.closeQuietly(fis);
            encoding = detector.getDetectedCharset();
            if ( encoding != null ){
                return encoding;
            }

            LOG.error("Unable to detect encoding");
            encoding = "UNK";
 
        } catch (IOException e ) {
            LOG.error("Encoding detection failed", e);
            encoding = "UNK";
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return encoding;
    }
}
