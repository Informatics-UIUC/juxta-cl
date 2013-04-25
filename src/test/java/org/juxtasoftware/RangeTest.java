package org.juxtasoftware;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.juxtasoftware.model.Range;

/**
 * Tests for the range model
 * 
 * @author loufoster
 *
 */
public class RangeTest {
    
    @Test
    public void basicTest() {
        Range r = new Range(1,10);
        assertTrue("start value incorrect", r.getStart() == 1);
        assertTrue("end value incorrect", r.getEnd() == 10);
        assertTrue("string representation incorrect", r.toString().contains("start=1, end=10"));
    }
    
    @Test
    public void testEquality() {
        Range r1 = new Range(1,10);
        Range r2 = new Range(10,20);
        Range r3 = new Range(1,10);
        assertTrue("equality incorrect", r1.equals(r3));
        assertTrue("inequality incorrect", (r1.equals(r2) == false) );
    }
    
    @Test
    public void testHashcode() {
        Range r1 = new Range(1,10);
        Range r2 = new Range(10,20);
        Range r3 = new Range(1,10);
        assertTrue("hashcode mismatch", r1.hashCode() == r3.hashCode());
        assertTrue("incorrect hashcode match", r1.hashCode() != r2.hashCode());
    }
    
    @Test
    public void testSort() {
        List<Range> list = new ArrayList<Range>();
        Range r1 = new Range(0,0);
        Range r2 = new Range(1,5);
        Range r3 = new Range(7,9);
        Range r4 = new Range(10,20);

        list.add( r4 );
        list.add( r3 );
        list.add( r2 );
        list.add( r1 );
        
        Collections.sort(list);
        assertTrue(list.get(0).equals(r1));
        assertTrue(list.get(1).equals(r2));
        assertTrue(list.get(2).equals(r3));
        assertTrue(list.get(3).equals(r4));
    }
}
