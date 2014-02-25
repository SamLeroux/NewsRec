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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JDBCViewsDao implements IViewsDao {

    private static BasicDataSource connectionPool = null;
    private PreparedStatement insertViewsStatement;
    private PreparedStatement selectStatement;
    private PreparedStatement selectTopNStatement;
    private static final Logger logger = Logger.getLogger(JDBCViewsDao.class);
    private static final ResourceBundle bundle = ResourceBundle.getBundle("newsRec");

    public JDBCViewsDao() throws ViewsDaoException {
        logger.debug("JDBCViewsDao constructor called");
        if (connectionPool == null) {
            logger.debug("creating connectionpool");
            String driver = bundle.getString("dbDriver");
            String user = bundle.getString("dbUser");
            String pass = bundle.getString("dbPass");
            String url = bundle.getString("dbUrl");
            url = url + "?user=" + user + "&password=" + pass;
            connectionPool = new BasicDataSource();
            connectionPool.setDriverClassName(driver);
            connectionPool.setUsername(user);
            connectionPool.setPassword(pass);
            connectionPool.setUrl(url);
            logger.debug("connectionpool created");
        }
        try {
            logger.debug("creating preparedstatements");
            
            String statementText = bundle.getString("selectViewsQuery");
            selectStatement = connectionPool.getConnection().prepareStatement(statementText);
            
            statementText = bundle.getString("insertUpdateViewsQuery");
            insertViewsStatement = connectionPool.getConnection().prepareStatement(statementText);
            
            statementText = bundle.getString("selectTopNViewsQuery");
            selectTopNStatement = connectionPool.getConnection().prepareStatement(statementText);
            
            logger.debug("created preparedstatements");
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new ViewsDaoException(ex);
        }
    }

    @Override
    public List<Integer> getSeenArticles(long userId) throws ViewsDaoException {
        try {
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
        }

    }

    @Override
    public void see(long userid, int docnr, long itemid) throws ViewsDaoException {
        try {
            logger.debug(userid + " has seen " + docnr);
            insertViewsStatement.setLong(1, userid);
            insertViewsStatement.setInt(2, docnr);
            insertViewsStatement.setLong(3, itemid);

            int result = insertViewsStatement.executeUpdate();
            logger.debug("return value insert/update: " + result);
        } catch (SQLException ex) {
            logger.error("Error updating rating", ex);
            throw new ViewsDaoException("Error inserting view: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Long> getNMostSeenArticles(int start, int stop) throws ViewsDaoException {
        try {
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
        }
    }

}
