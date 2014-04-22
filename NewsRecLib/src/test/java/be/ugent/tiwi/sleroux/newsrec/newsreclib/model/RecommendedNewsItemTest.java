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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecommendedNewsItemTest extends TestCase {
    
    public RecommendedNewsItemTest(String testName) {
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
     * Test of compareTo method, of class RecommendedNewsItem.
     */
    public void testCompareTo() {
        RecommendedNewsItem r1 = new RecommendedNewsItem();
        r1.setScore(1F);
        RecommendedNewsItem r2 = new RecommendedNewsItem();
        r2.setScore(2F);
        RecommendedNewsItem r3 = new RecommendedNewsItem();
        r3.setScore(1F);
        
        assertTrue(r1.compareTo(r2) < 0);
        assertTrue(r2.compareTo(r1) > 0);
        assertTrue(r3.compareTo(r1) == 0);
        
        List<RecommendedNewsItem> items = new ArrayList<>();
        items.add(r1);
        items.add(r2);
        items.add(r3);
        Collections.sort(items);
        assertTrue(items.get(0) == r1 | items.get(0) == r3);
        if (items.get(0) == r1) {
            assertTrue(items.get(1) == r3);
        } else {
            assertTrue(items.get(1) == r1);
        }
        assertTrue(items.get(2) == r2);
        
    }
    
}
