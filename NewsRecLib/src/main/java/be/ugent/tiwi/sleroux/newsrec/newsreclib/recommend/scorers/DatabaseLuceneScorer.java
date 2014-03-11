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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.topTerms.LuceneDocTopTermsExtract;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Updates the user model stored in a database with the information of the
 * viewed item stored in the Lucene index.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DatabaseLuceneScorer implements IScorer {

    private final IRatingsDao ratingsDao;

    private final LuceneDocTopTermsExtract termExtract;
    private static final Logger logger = Logger.getLogger(DatabaseLuceneScorer.class);

    /**
     *
     * @param lucineIndexLocation Location where the index is stored
     * @param dao The RatingsDao to use
     * @throws IOException when there was an error opening the Lucene index.
     */
    public DatabaseLuceneScorer(String lucineIndexLocation, IRatingsDao dao) throws IOException {
        ratingsDao = dao;
        termExtract = new LuceneDocTopTermsExtract(lucineIndexLocation);
    }

    @Override
    public void score(long user, int item, double rating) {

        Map<String, Double> termsToStore = termExtract.getTopTerms(item);
        for (String term : termsToStore.keySet()) {
            termsToStore.put(term, rating * termsToStore.get(term));
        }
        try {
            ratingsDao.giveRating(user, termsToStore);
        } catch (RatingsDaoException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    @Override
    public void view(long user, int item) {
        score(user, item, 0.75);

    }

}
