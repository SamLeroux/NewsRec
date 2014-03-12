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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders;

import java.io.IOException;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

/**
 * A custom query wrapper, gives recent documents a larger boost. The boost
 * factor (bf) is calculated with the formula bf = a/(m*x + b) x is the age of
 * the article in milliseconds, a, m and b are constants. The values used are:
 * a=1.1, b=1.0 and m=9e-10. The returned score is the score of the inner query
 * * bf
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecencyBoostQuery extends CustomScoreQuery {

    private double a = 1.1;
    private double b = 1.0;
    private double m = 9e-10;
    private static final Logger logger = Logger.getLogger(RecencyBoostQuery.class);

    /**
     *
     * @param subQuery The inner query
     */
    public RecencyBoostQuery(Query subQuery) {
        super(subQuery);
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        return new RecencyBoostScoreProvider(context);
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getM() {
        return m;
    }

    public void setM(double m) {
        this.m = m;
    }

    private class RecencyBoostScoreProvider extends CustomScoreProvider {

        private final AtomicReader atomicReader;

        public RecencyBoostScoreProvider(AtomicReaderContext context) {
            super(context);
            atomicReader = context.reader();
        }

        @Override
        public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
            float score = super.customScore(doc, subQueryScore, valSrcScore);
            IndexableField f = atomicReader.document(doc).getField("timestamp");
            if (f != null) {
                logger.info(doc);
                Number numericValue = f.numericValue();
                if (numericValue != null) {
                    float timestamp = numericValue.longValue();
                    Date now = new Date();
                    float diff = now.getTime() - timestamp;
                    score *= (float) (a / (diff * m + b));
                } else {
                    logger.warn("timestamp field for document with docNr="
                            + doc
                            + " does not contain a numeric value,"
                            + "no custom recency boost for this document");
                }
            } else {
                logger.warn("No timestamp found in document with docNr=" + doc
                        + ", no custom recencyboost for this document");
            }
            return score;
        }

    }

}
