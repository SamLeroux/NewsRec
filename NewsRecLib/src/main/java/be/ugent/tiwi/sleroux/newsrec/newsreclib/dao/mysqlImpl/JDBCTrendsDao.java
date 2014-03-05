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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import static be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.AbstractJDBCBaseDao.logger;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JDBCTrendsDao extends AbstractJDBCBaseDao implements ITrendsDao, Serializable {

    private transient PreparedStatement selectStatement;
    private transient PreparedStatement insertStatement;

    public JDBCTrendsDao() throws DaoException {
    }

    @Override
    public String[] getTrends() throws TrendsDaoException {
        logger.debug("Get trends");
        try {
            selectStatement.setInt(1, 25);
            try (ResultSet results = selectStatement.executeQuery()) {
                List<String> trends = new ArrayList<>(25);
                while (results.next()) {
                    trends.add(results.getString(1));
                }
                return trends.toArray(new String[trends.size()]);
            }
        } catch (SQLException ex) {
            throw new TrendsDaoException(ex);
        }

    }

    @Override
    public void updateTrends(String[] trends) throws TrendsDaoException {
        try {
            logger.debug("update trends");
            int i = 0;
            for (String trend : trends) {
                insertStatement.setString(1, trend);
                insertStatement.setInt(2, i);
                insertStatement.setInt(3, i);
                i++;
                insertStatement.addBatch();
            }
            int[] result = insertStatement.executeBatch();
            logger.debug("return value insert/update: " + result);
        } catch (SQLException ex) {
            logger.error("Error updating rating", ex);
            throw new TrendsDaoException(ex);
        }
    }

    @Override
    protected void createStatements() throws DaoException {
        try {
            String selectText = bundle.getString("selectTopTermsQuery");
            selectStatement = getConnection().prepareStatement(selectText);

            String insertText = bundle.getString("insertUpdateTrendsQuery");
            insertStatement = getConnection().prepareStatement(insertText);
        } catch (SQLException ex) {
            throw new TrendsDaoException(ex);
        }
    }

    @Override
    protected void closeStatements() throws DaoException {
        try {
            selectStatement.close();
            insertStatement.close();
        } catch (SQLException ex) {
            throw new TrendsDaoException(ex);
        }
    }

}
