package org.juxtasoftware.model;

/**
 * Model to store and compare offset range information
 * @author loufoster
 *
 */
public class Range implements Comparable<Range> {
    private final long start;
    private final long end;
    
    public Range(long s, long e) {
        this.start = s;
        this.end = e;
    }
    
    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    @Override
    public int compareTo(Range that) {
        if ( this.getStart() < that.getStart() ) {
            return -1;
        } else if ( this.getStart() > that.getStart() ) {
            return 1;
        } else {
            if ( this.getEnd() < that.getEnd() ) {
                return -1;
            } else if ( this.getEnd() > that.getEnd() ) {
                return 1;
            } 
        }
        return 0;
    }  

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (end ^ (end >>> 32));
        result = prime * result + (int) (start ^ (start >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Range other = (Range) obj;
        if (end != other.end)
            return false;
        if (start != other.start)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Range [start=" + start + ", end=" + end + "]";
    }
}
