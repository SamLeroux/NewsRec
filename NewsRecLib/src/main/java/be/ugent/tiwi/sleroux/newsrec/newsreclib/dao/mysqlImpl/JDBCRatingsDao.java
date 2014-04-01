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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.ScoreDecay;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JDBCRatingsDao extends AbstractJDBCBaseDao implements IRatingsDao {

    private final ScoreDecay decay;
    public JDBCRatingsDao() throws DaoException {
        decay = new ScoreDecay();
    }

    @Override
    public Map<String, Double> getRatings(long userid) throws RatingsDaoException {
        PreparedStatement selectStatement = null;
        Connection conn = null;
        try {
            logger.debug("get rating for user: " + userid);
            String statementText = bundle.getString("selectRatingsQuery");
            conn = getConnection();
            conn.setAutoCommit(true);
            selectStatement = conn.prepareStatement(statementText);
            selectStatement.setLong(1, userid);
            Map<String, Double> ratings;
            try (ResultSet results = selectStatement.executeQuery()) {
                ratings = new HashMap<>();
                while (results.next()) {
                    Date lastChanged = results.getDate(3);
                    double score = results.getDouble(2);
                    score *= decay.getBoost(new Date().getTime() - lastChanged.getTime());
                    ratings.put(results.getString(1), score );
                }
            }
            return ratings;
        } catch (SQLException ex) {
            logger.error("Error fetching ratings", ex);
            throw new RatingsDaoException("Error fetching ratings: " + ex.getMessage(), ex);
        } finally {
            try {
                if (selectStatement != null) {
                    selectStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new RatingsDaoException(ex);
            }
        }

    }

    @Override
    public void giveRating(long userid, String term, double rating) throws RatingsDaoException {
        PreparedStatement insertUpdateRatingStatement = null;
        Connection conn = null;
        try {
            logger.debug("set rating for user: " + userid + " and term: " + term + " to: " + rating);
            String statementText = bundle.getString("insertUpdateRatingsQuery");
            conn = getConnection();

            insertUpdateRatingStatement = conn.prepareStatement(statementText);

            insertUpdateRatingStatement.setLong(1, userid);
            insertUpdateRatingStatement.setString(2, term);
            insertUpdateRatingStatement.setDouble(3, rating);
            insertUpdateRatingStatement.setDouble(4, rating);

            int result = insertUpdateRatingStatement.executeUpdate();
            logger.debug("return value insert/update: " + result);
        } catch (SQLException ex) {
            logger.error("Error updating rating", ex);
            throw new RatingsDaoException("Error updating rating: " + ex.getMessage(), ex);
        } finally {
            try {
                if (insertUpdateRatingStatement != null) {
                    insertUpdateRatingStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new RatingsDaoException(ex);
            }
        }
    }

    @Override
    public void giveRating(long userid, Map<String, Double> terms) throws RatingsDaoException {
        PreparedStatement insertUpdateRatingStatement = null;
        Connection conn = null;
        try {
            logger.debug("updating ratings in batch");
            String statementText = bundle.getString("insertUpdateRatingsQuery");
            conn = getConnection();

            insertUpdateRatingStatement = conn.prepareStatement(statementText);

            for (String term : terms.keySet()) {
                double rating = terms.get(term);
                insertUpdateRatingStatement.setLong(1, userid);
                insertUpdateRatingStatement.setString(2, term);
                insertUpdateRatingStatement.setDouble(3, rating);
                insertUpdateRatingStatement.setDouble(4, rating);
                insertUpdateRatingStatement.addBatch();
            }

            int[] result = insertUpdateRatingStatement.executeBatch();
            logger.debug("length return value insert/update: " + result.length);
        } catch (SQLException ex) {
            logger.error("Error updating rating", ex);
            throw new RatingsDaoException("Error updating rating: " + ex.getMessage(), ex);
        } finally {
            try {
                if (insertUpdateRatingStatement != null) {
                    insertUpdateRatingStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new RatingsDaoException(ex);
            }
        }
    }

}
