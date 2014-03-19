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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JDBCViewsDao extends AbstractJDBCBaseDao implements IViewsDao {

    public JDBCViewsDao() throws DaoException {
    }

    @Override
    public List<Integer> getSeenArticles(long userId) throws ViewsDaoException {
        Connection conn = null;
        PreparedStatement selectStatement = null;

        try {
            conn = getConnection();
            String statementText = bundle.getString("selectViewsQuery");
            selectStatement = conn.prepareStatement(statementText);

            selectStatement.setLong(1, userId);

            List<Integer> seen = new ArrayList<>();
            try (ResultSet results = selectStatement.executeQuery()) {
                while (results.next()) {
                    seen.add(results.getInt(1));
                }
            } catch (SQLException ex) {
                logger.error(ex);
            }

            return seen;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new ViewsDaoException(ex);
        } finally {
            try {
                if (selectStatement != null) {
                    selectStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new ViewsDaoException(ex);
            }
        }

    }

    @Override
    public void see(long userid, int docnr, String itemid) throws ViewsDaoException {
        Connection conn = null;
        PreparedStatement insertViewsStatement = null;
        try {
            logger.debug(userid + " has seen " + docnr);

            conn = getConnection();
            String statementText = bundle.getString("insertUpdateViewsQuery");
            insertViewsStatement = conn.prepareStatement(statementText);

            insertViewsStatement.setLong(1, userid);
            insertViewsStatement.setInt(2, docnr);
            insertViewsStatement.setLong(3, Long.parseLong(itemid));

            int result = insertViewsStatement.executeUpdate();
            logger.debug("return value insert/update: " + result);

        } catch (SQLException ex) {
            logger.error("Error updating rating", ex);
            throw new ViewsDaoException("Error inserting view: " + ex.getMessage(), ex);
        } finally {
            try {
                if (insertViewsStatement != null) {
                    insertViewsStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new ViewsDaoException(ex);
            }
        }
    }

    @Override
    public List<Long> getNMostSeenArticles(int start, int stop) throws ViewsDaoException {
        Connection conn = null;
        PreparedStatement selectTopNStatement = null;

        try {
            conn = getConnection();
            String statementText = bundle.getString("selectTopNViewsQuery");
            selectTopNStatement = conn.prepareStatement(statementText);

            selectTopNStatement.setInt(1, start);
            selectTopNStatement.setInt(2, stop);
            List<Long> out = new ArrayList<>();

            try (ResultSet results = selectTopNStatement.executeQuery()) {
                while (results.next()) {
                    out.add(results.getLong(1));
                }
            } catch (SQLException ex) {
                logger.error(ex);
            }

            return out;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new ViewsDaoException(ex);
        } finally {
            try {
                if (selectTopNStatement != null) {
                    selectTopNStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new ViewsDaoException(ex);
            }
        }
    }

}
