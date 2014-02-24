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
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecencyBoostQuery extends CustomScoreQuery {

    public RecencyBoostQuery(Query subQuery) {
        super(subQuery);
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        return new RecencyBoostScoreProvider(context);
    }

    private class RecencyBoostScoreProvider extends CustomScoreProvider {

        private final AtomicReader atomicReader;
        private double a = 1.0;
        private double b = 1.0;
        private double m = 9e-10;

        public RecencyBoostScoreProvider(AtomicReaderContext context) {
            super(context);
            atomicReader = context.reader();
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

        @Override
        public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
            float score = super.customScore(doc, subQueryScore, valSrcScore);
            float timestamp = atomicReader.document(doc).getField("timestamp").numericValue().longValue();
            Date now = new Date();
            float diff = now.getTime() - timestamp;
            score *= (float) (a / (diff * m + b));
            return score;
        }

    }

}
