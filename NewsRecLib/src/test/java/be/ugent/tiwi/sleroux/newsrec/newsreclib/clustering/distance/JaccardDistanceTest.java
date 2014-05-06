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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.RecommendedNewsItem;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JaccardDistanceTest extends TestCase {

    public JaccardDistanceTest(String testName) {
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
     * Test of distance method, of class JaccardDistance.
     */
    public void testDistance() {
        List<RecommendedNewsItem> items = new ArrayList<>();
        RecommendedNewsItem r1 = new RecommendedNewsItem();
        r1.addTerm("abc", 0.4);
        r1.addTerm("fefe", 0.9);
        r1.addTerm("cedcd", 0.5);
        r1.addTerm("abdzdzc", 0.3);
        r1.addTerm("dzdzdz", 0.8);
        r1.addTerm("v", 1);

        RecommendedNewsItem r2 = new RecommendedNewsItem();
        r2.addTerm("abc", 0.4);
        r2.addTerm("fefe", 0.9);
        r2.addTerm("cedcd", 0.5);
        r2.addTerm("abdzdzc", 0.3);
        r2.addTerm("dzdzdz", 0.8);
        r2.addTerm("v", 1);

        RecommendedNewsItem r3 = new RecommendedNewsItem();
        r3.addTerm("abc", 0.4);
        r3.addTerm("fefe", 0.9);
        r3.addTerm("cedcd", 0.5);
        r3.addTerm("dddddddd", 0.3);
        r3.addTerm("dzdzdz", 0.8);
        r3.addTerm("v", 1);

        items.add(r1);
        items.add(r2);
        items.add(r3);

        for (int i = 0; i < items.size(); i++) {
            items.get(i).setRecommendationId(i);
        }

        JaccardDistance d = new JaccardDistance(items);
        double d1 = d.distance(r1, r2);
        double d2 = d.distance(r2, r1);
        assertEquals(d1, d2);

        double d3 = d.distance(r1, r3);
        double d4 = d.distance(r2, r3);
        System.out.println(d1);
        System.out.println(d3);
        assertEquals(d3, d4);
        assertTrue(d1 < d3);

    }

}
