package org.juxtasoftware;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the Juxta CL
 * @author lfoster
 *
 */
public class Configuration {
    public enum Hyphens {ALL, LINEBREAK, NONE}
    public enum Mode {VERSION, HELP, TRANSFORM, TOKENIZE, CHANGE_INDEX};
    
    private List<String> files = new ArrayList<String>();
    private boolean ignorePunctuation = true;
    private boolean ignoreCase = true;
    private boolean verbose = false;
    private Hyphens hyphenationFilter = Hyphens.ALL; 
    private Mode mode = Mode.CHANGE_INDEX;
    
    public void addFile(String file) {
        if ( this.files.size() < 2 ) {
            this.files.add(file);
        } else {
            throw new RuntimeException("Only 2 files can be accepted");
        }
    }
    
    public void setVerbose( boolean val ) {
        this.verbose = val;
    }
   
    public void setIgnorePunctuation( boolean val ) {
        this.ignorePunctuation = val;
    }
    
    public void setIgnoreCase( boolean val ) {
        this.ignoreCase = val;
    }
    
    public void setHyphenation( Hyphens f ) {
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
    
    public boolean isVerbose() {
        return this.verbose;
    }

    public Hyphens getHyphenation() {
        return this.hyphenationFilter;
    }
    
    public void setMode( Mode mode ) {
        this.mode = mode;
    }
    
    public Mode getMode() {
        return this.mode;
    }

    @Override
    public String toString() {
        return "Configuration [files=" + files + ", ignorePunctuation=" + ignorePunctuation + ", ignoreCase="
            + ignoreCase + ", verbose=" + verbose + ", hyphenationFilter=" + hyphenationFilter + "]";
    }
    
}
