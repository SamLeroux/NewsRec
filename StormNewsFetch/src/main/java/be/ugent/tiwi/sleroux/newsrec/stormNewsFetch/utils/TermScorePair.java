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

import java.util.Objects;

/**
 * Contains a Term (String) and a score attached to this term (double).
 * TermScorePairs are comparable based on the scores. TermScorePairs with
 * larger scores come before TermScorePairs with smaller scores in the natural ordering.
 * {"abc", 2.5} < {"abc"}, 1.2}.
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TermScorePair implements Comparable<Object> {

    private String term;
    private double score;

    /**
     *
     * @param term
     * @param score
     */
    public TermScorePair(String term, double score) {
        this.term = term;
        this.score = score;
    }

    /**
     *
     * @return
     */
    public String getTerm() {
        return term;
    }

    /**
     *
     * @param term
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     *
     * @return
     */
    public double getScore() {
        return score;
    }

    /**
     *
     * @param score
     */
    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof TermScorePair) {
            return Double.compare(((TermScorePair) o).score, score);
        }
        return -1;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.term);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TermScorePair other = (TermScorePair) obj;
        return Double.doubleToLongBits(this.score) == Double.doubleToLongBits(other.score);
    }
    
    

}
