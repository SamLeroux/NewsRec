/*
 * Copyright 2014 Sam Leroux <sam.leroux@ugent.be>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.utils;

import junit.framework.TestCase;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TermScorePairTest extends TestCase {

    public TermScorePairTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getTerm method, of class TermScorePair.
     */
    public void testGetTerm() {
        TermScorePair tsp = new TermScorePair("abc", 0.87);
        assertEquals(tsp.getTerm(), "abc");
    }

    /**
     * Test of setTerm method, of class TermScorePair.
     */
    public void testSetTerm() {
        TermScorePair tsp = new TermScorePair("abc", 0.87);
        tsp.setTerm("def");
        assertEquals(tsp.getTerm(), "def");
    }

    /**
     * Test of getScore method, of class TermScorePair.
     */
    public void testGetScore() {
        TermScorePair tsp = new TermScorePair("abc", 0.87);
        assertEquals(tsp.getScore(), 0.87);
    }

    /**
     * Test of setScore method, of class TermScorePair.
     */
    public void testSetScore() {
        TermScorePair tsp = new TermScorePair("abc", 0.87);
        tsp.setScore(9.98);
        assertEquals(tsp.getScore(), 9.98);
    }

    /**
     * Test of compareTo method, of class TermScorePair.
     */
    public void testCompareTo() {
       TermScorePair tsp1 = new TermScorePair("abc", 1);
       TermScorePair tsp2 = new TermScorePair("def", 2);
        assertTrue(tsp1.compareTo(tsp2) > 0);
        assertTrue(tsp2.compareTo(tsp1) < 0);
        assertTrue(tsp1.compareTo(tsp1) == 0);
        
    }

}
