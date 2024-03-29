package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.dummyImpl;

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


import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.RatingsDaoException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DummyRatingsDao implements IRatingsDao{
    private final Map<String, Double> ratings;

    /**
     *
     */
    public DummyRatingsDao() {
        ratings = new HashMap<>();
    }

    /**
     *
     * @param userid
     * @return
     * @throws RatingsDaoException
     */
    @Override
    public Map<String, Double> getRatings(long userid) throws RatingsDaoException {
        return ratings;
    }

    /**
     *
     * @param userid
     * @param term
     * @param rating
     * @throws RatingsDaoException
     */
    @Override
    public void giveRating(long userid, String term, double rating) throws RatingsDaoException {
        ratings.put(term, rating);
    }

    /**
     *
     * @param userid
     * @param terms
     * @throws RatingsDaoException
     */
    @Override
    public void giveRating(long userid, Map<String, Double> terms) throws RatingsDaoException {
        ratings.putAll(terms);
    }
    
}
