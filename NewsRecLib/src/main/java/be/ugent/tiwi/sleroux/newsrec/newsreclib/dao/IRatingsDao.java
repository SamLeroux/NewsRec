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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao;

import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public interface IRatingsDao {

    public Map<String, Double> getRatings(long userid) throws RatingsDaoException;

    public Map<Long, Map<String, Double>> getAllRatings() throws RatingsDaoException;

    public void giveRating(long userid, String term, double rating) throws RatingsDaoException;

    public void giveRating(long userid, Map<String, Double> terms) throws RatingsDaoException;

    public void close() throws DaoException;
}
