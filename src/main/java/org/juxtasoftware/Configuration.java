package org.juxtasoftware;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the Juxta CL
 * @author lfoster
 *
 */
public class Configuration {
    public enum HyphenationFilter {INCLUDE_ALL, FILTER_LINEBREAK, FILTER_ALL}
    
    private List<String> files = new ArrayList<String>();
    private boolean ignorePunctuation = true;
    private boolean ignoreCase = true;
    private HyphenationFilter hyphenationFilter = HyphenationFilter.INCLUDE_ALL; 
    
    public void addFile(String file) {
        if ( this.files.size() < 2 ) {
            this.files.add(file);
        }
        throw new RuntimeException("Only 2 files can be accepted");
    }
    
    public void setIgnorePunctuation( boolean val ) {
        this.ignorePunctuation = val;
    }
    
    public void setIgnoreCase( boolean val ) {
        this.ignoreCase = val;
    }
    
    public void setHyphenationFilter( HyphenationFilter f ) {
        this.hyphenationFilter = f;
    }

    public List<String> getFiles() {
        return this.files;
    }

    public boolean isIgnorePunctuation() {
        return this.ignorePunctuation;
    }

    public boolean isIgnoreCase() {
        return this.ignoreCase;
    }

    public HyphenationFilter getHyphenationFilter() {
        return this.hyphenationFilter;
    }
}
