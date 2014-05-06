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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.utils;

import junit.framework.TestCase;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class HashCircularBufferTest extends TestCase {

    public HashCircularBufferTest(String testName) {
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
     * Test of put method, of class HashCircularBuffer.
     */
    public void testPut() {
        System.out.println("put");

        HashCircularBuffer<Integer> buffer = new HashCircularBuffer<>(20);
        assertEquals(buffer.capacity(), 20);

        buffer.put(5);
        buffer.put(5);
        buffer.put(5);
        assertEquals(buffer.size(), 1);

        for (int i = 0; i < 5; i++) {
            buffer.put(i);
        }
        assertEquals(buffer.size(), 6);

        for (int i = 6; i < 30; i++) {
            buffer.put(i);
        }

        assertEquals(buffer.size(), 20);

    }

    /**
     * Test of putNoCheck method, of class HashCircularBuffer.
     */
    public void testPutNoCheck() {

    }

    /**
     * Test of contains method, of class HashCircularBuffer.
     */
    public void testContains() {
        HashCircularBuffer<Integer> buffer = new HashCircularBuffer<>(20);
        for (int i = 0; i < 50; i++) {
            buffer.put(i);
        }
        for (int i = 30; i < 50; i++) {
            assertTrue(buffer.contains(i));
        }
        buffer.put(56565);
        assertFalse(buffer.contains(30));
        assertTrue(buffer.contains(56565));
    }

}
