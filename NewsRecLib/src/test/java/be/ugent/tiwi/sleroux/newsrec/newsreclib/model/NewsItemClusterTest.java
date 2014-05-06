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

import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsItemClusterTest extends TestCase {

    public NewsItemClusterTest(String testName) {
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
     * Test of addItem method, of class NewsItemCluster.
     */
    public void testAddItem() {
        RecommendedNewsItem item = new RecommendedNewsItem();
        NewsItemCluster instance = new NewsItemCluster();
        instance.addItem(item);
        assertTrue(instance.getRepresentative() == item);
        assertTrue(instance.getItems().isEmpty());

        instance.addItem(item);
        assertTrue(instance.getRepresentative() == item);
        assertFalse(instance.getItems().isEmpty());

    }

    /**
     * Test of getSize method, of class NewsItemCluster.
     */
    public void testGetSize() {
        RecommendedNewsItem item = new RecommendedNewsItem();
        NewsItemCluster instance = new NewsItemCluster();
        for (int i = 0; i < 5; i++) {
            instance.addItem(item);
        }
        assertTrue(instance.getSize() == 5);

    }

    public void testCorrectRepresentative() {
        NewsItemCluster c = new NewsItemCluster();

        RecommendedNewsItem r1 = new RecommendedNewsItem();
        r1.setTimestamp(new Date());
        c.addItem(r1);

        assertTrue(c.getRepresentative() == r1);
        assertTrue(c.getItems().isEmpty());

        RecommendedNewsItem r2 = new RecommendedNewsItem();
        r2.setTimestamp(new Date(System.currentTimeMillis() + 200000));
        c.addItem(r2);

        assertTrue(c.getRepresentative() == r2);
        assertFalse(c.getItems().isEmpty());
        assertTrue(c.getItems().get(0) == r1);
    }

}
