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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JDBCRatingsDao extends AbstractJDBCBaseDao implements IRatingsDao {

    private PreparedStatement insertUpdateRatingStatement;
    private PreparedStatement selectStatement;

    public JDBCRatingsDao() throws DaoException {
    }

    @Override
    public Map<String, Double> getRatings(long userid) throws RatingsDaoException {
        try {
            logger.debug("get rating for user: " + userid);
            selectStatement.setLong(1, userid);
            Map<String, Double> ratings;
            try (ResultSet results = selectStatement.executeQuery()) {
                ratings = new HashMap<>();
                while (results.next()) {
                    ratings.put(results.getString(1), results.getDouble(2));
                }
            }
            return ratings;
        } catch (SQLException ex) {
            logger.error("Error fetching ratings", ex);
            throw new RatingsDaoException("Error fetching ratings: " + ex.getMessage(), ex);
        }

    }

    @Override
    public void giveRating(long userid, String term, double rating) throws RatingsDaoException {
        try {
            logger.debug("set rating for user: " + userid + " and term: " + term + " to: " + rating);
            insertUpdateRatingStatement.setLong(1, userid);
            insertUpdateRatingStatement.setString(2, term);
            insertUpdateRatingStatement.setDouble(3, rating);
            insertUpdateRatingStatement.setDouble(4, rating);

            int result = insertUpdateRatingStatement.executeUpdate();
            logger.debug("return value insert/update: " + result);
        } catch (SQLException ex) {
            logger.error("Error updating rating", ex);
            throw new RatingsDaoException("Error updating rating: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void giveRating(long userid, Map<String, Double> terms) throws RatingsDaoException {
        try {
            logger.debug("updating ratings in batch");
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
        }
    }

    @Override
    protected void createStatements() throws DaoException {
        try {
            logger.debug("creating preparedstatements");
            String statementText = bundle.getString("selectRatingsQuery");
            Connection conn = getConnection();
            conn.setAutoCommit(true);
            selectStatement = conn.prepareStatement(statementText);
            statementText = bundle.getString("insertUpdateRatingsQuery");
            insertUpdateRatingStatement = conn.prepareStatement(statementText);
            logger.debug("created preparedstatements");
        } catch (SQLException ex) {
            throw new RatingsDaoException(ex);
        }
    }

    @Override
    protected void closeStatements() throws DaoException {
        try {
            selectStatement.close();
            insertUpdateRatingStatement.close();
        } catch (SQLException ex) {
            throw new RatingsDaoException(ex);
        }
    }

}