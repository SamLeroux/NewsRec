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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Fixed size FIFO buffer with constant time push(), pop() and contains()
 * operations.
 * Duplicates are not allowed.
 * @author Sam Leroux <sam.leroux@ugent.be>
 * @param <T>
 */
public class HashCircularBuffer<T> implements Serializable{
    private final Queue<T> queue;
    private final Set<T> set;
    private final int capacity;
    
    /**
     *
     * @param capacity The fixed size of the buffer
     */
    public HashCircularBuffer(int capacity) {
        this.capacity = capacity;
        queue = new ArrayBlockingQueue<>(capacity);
        set = new HashSet<>(capacity);
    }
    
    /**
     * Adds a new item to the queue if it does not already exists in the queue.
     * @param t The item to add to the buffer
     */
    public synchronized void put(T t){
        if (!set.contains(t)){
            putNoCheck(t);
        }
    }
    
    /**
     * Adds a new item to the queue. Does not check if the item already exists in
     * the queue. Does nothing if the item already exists in the queue.
     * @param t
     */
    public synchronized void putNoCheck(T t){
        if (queue.size() == capacity){
            T delete = queue.remove();
            set.remove(delete);
        }
        set.add(t);
        queue.offer(t);
    }
    
    /**
     * Checks if the queue contains this item.
     * @param t
     * @return
     */
    public boolean contains(T t){
        return set.contains(t);
    }
    
    /**
     *
     * @return
     */
    public int size(){
        return queue.size();
    }
    
    /**
     *
     * @return
     */
    public int capacity(){
        return this.capacity;
    }
    
    
    
    
}
