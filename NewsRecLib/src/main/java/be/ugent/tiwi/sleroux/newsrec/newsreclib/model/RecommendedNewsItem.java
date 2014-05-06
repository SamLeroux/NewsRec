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

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecommendedNewsItem extends NewsItem implements Comparable<RecommendedNewsItem> {

    private String recommendedBy;
    private int docNr;
    private float score;
    private int recommendationId;

    public RecommendedNewsItem() {
        super();
    }

    public RecommendedNewsItem(String recommendedBy, int docNr, float score) {
        this.recommendedBy = recommendedBy;
        this.docNr = docNr;
        this.score = score;
    }

    public RecommendedNewsItem(String recommendedBy, int docNr, float score, String title, List<String> authors, String fulltext, String description, Date timestamp, Map<String, Double> terms, Locale locale, String source, URL url, URL imageUrl) {
        super(title, authors, fulltext, description, timestamp, terms, locale, source, url, imageUrl);
        this.recommendedBy = recommendedBy;
        this.docNr = docNr;
        this.score = score;
    }

    public RecommendedNewsItem(String recommendedBy, int docNr, float score, String id, String title, List<String> authors, String fulltext, String description, Date timestamp, Map<String, Double> terms, Locale locale, String source, URL url, URL imageUrl) {
        super(id, title, authors, fulltext, description, timestamp, terms, locale, source, url, imageUrl);
        this.recommendedBy = recommendedBy;
        this.docNr = docNr;
        this.score = score;
    }

    public String getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(String recommendedBy) {
        this.recommendedBy = recommendedBy;
    }

    public int getDocNr() {
        return docNr;
    }

    public void setDocNr(int docNr) {
        this.docNr = docNr;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public int compareTo(RecommendedNewsItem o) {
        if (getRecommendedBy() != null && o.getRecommendedBy() != null && !getRecommendedBy().equals(o.getRecommendedBy())) {
            if (o.getRecommendedBy().equals("trending")) {
                return Float.compare(score, o.getScore() * 2);
            } else {
                return Float.compare(2 * score, o.getScore());
            }
        }

        return Float.compare(score, o.getScore());
    }

    public int getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(int recommendationId) {
        this.recommendationId = recommendationId;
    }

}
