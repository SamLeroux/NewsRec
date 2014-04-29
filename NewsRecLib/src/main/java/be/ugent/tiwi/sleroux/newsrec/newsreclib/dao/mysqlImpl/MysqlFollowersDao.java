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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITwitterFollowersDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import static be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.AbstractJDBCBaseDao.logger;
import java.io.Serializable;
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
public class MysqlFollowersDao extends AbstractJDBCBaseDao implements ITwitterFollowersDao, Serializable {

    public MysqlFollowersDao() throws DaoException {
    }

    @Override
    public List<Long> getUsersToFollow() throws DaoException {
        PreparedStatement selectStatement = null;
        Connection conn = null;
        try {
            logger.debug("get users to follow");

            String statementText = bundle.getString("selectFollowersQuery");

            conn = getConnection();
            conn.setAutoCommit(true);
            selectStatement = conn.prepareStatement(statementText);

            List<Long> users = new ArrayList<>();
            try (ResultSet results = selectStatement.executeQuery()) {
                while (results.next()) {
                    long user = results.getLong(1);
                    users.add(user);
                }
            }
            return users;
        } catch (SQLException ex) {
            logger.error("Error fetching users to follow", ex);
            throw new DaoException(ex);
        } finally {
            try {
                if (selectStatement != null) {
                    selectStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new DaoException(ex);
            }
        }
    }

    @Override
    public void addUserToFollow(String screenName, long userid) throws DaoException {
        PreparedStatement insertStatement = null;
        Connection conn = null;
        try {
            logger.debug("insert user to follow");
            String statementText = bundle.getString("insertFollowerQuery");
            conn = getConnection();

            insertStatement = conn.prepareStatement(statementText);

            insertStatement.setString(1, screenName);
            insertStatement.setLong(2, userid);

            int result = insertStatement.executeUpdate();
            logger.debug("return value insert/update: " + result);
        } catch (SQLException ex) {
            logger.error("Error inserting follower", ex);
            throw new DaoException("Error inserting follower: " + ex.getMessage(), ex);
        } finally {
            try {
                if (insertStatement != null) {
                    insertStatement.close();
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
