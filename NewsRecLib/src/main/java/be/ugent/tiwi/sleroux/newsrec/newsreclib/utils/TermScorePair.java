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

/**
 * Wrapper around a term (string) and a score (double). Instances are comparable
 * based on the score.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TermScorePair implements Comparable<Object> {

    private String term;
    private double score;

    public TermScorePair(String term, double score) {
        this.term = term;
        this.score = score;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public double getScore() {
        return score;
    }

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

}
