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

package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.storm.bolts.trendDetect;

import backtype.storm.utils.Time;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

/**
* This class tracks the time-since-last-modify of a "thing" in a rolling fashion.
* <p/>
* For example, create a 5-slot tracker to track the five most recent time-since-last-modify.
* <p/>
* You must manually "mark" that the "something" that you want to track -- in terms of modification times -- has just
* been modified.
*/
public class NthLastModifiedTimeTracker {

  private static final int MILLIS_IN_SEC = 250;

  private final CircularFifoBuffer lastModifiedTimesMillis;

    /**
     *
     * @param numTimesToTrack
     */
    public NthLastModifiedTimeTracker(int numTimesToTrack) {
    if (numTimesToTrack < 1) {
      throw new IllegalArgumentException(
          "numTimesToTrack must be greater than zero (you requested " + numTimesToTrack + ")");
    }
    lastModifiedTimesMillis = new CircularFifoBuffer(numTimesToTrack);
    initLastModifiedTimesMillis();
  }

  private void initLastModifiedTimesMillis() {
    long nowCached = now();
    for (int i = 0; i < lastModifiedTimesMillis.maxSize(); i++) {
      lastModifiedTimesMillis.add(Long.valueOf(nowCached));
    }
  }

  private long now() {
    return Time.currentTimeMillis();
  }

    /**
     *
     * @return
     */
    public int secondsSinceOldestModification() {
    long modifiedTimeMillis = ((Long) lastModifiedTimesMillis.get()).longValue();
    return (int) ((now() - modifiedTimeMillis) / MILLIS_IN_SEC);
  }

    /**
     *
     */
    public void markAsModified() {
    updateLastModifiedTime();
  }

  private void updateLastModifiedTime() {
    lastModifiedTimesMillis.add(now());
  }

}
