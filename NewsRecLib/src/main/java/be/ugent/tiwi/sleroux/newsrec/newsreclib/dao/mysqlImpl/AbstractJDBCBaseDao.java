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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public abstract class AbstractJDBCBaseDao {

    private static BasicDataSource connectionPool = null;
    protected static final Logger logger = Logger.getLogger(JDBCViewsDao.class);
    protected static final ResourceBundle bundle = ResourceBundle.getBundle("newsRec");

    public AbstractJDBCBaseDao() throws DaoException {
        if (connectionPool == null) {
            connectionPool = createConnectionPool();
        }
    }

    private synchronized BasicDataSource createConnectionPool() {
        BasicDataSource source;
        logger.debug("creating connectionpool");
        String driver = bundle.getString("dbDriver");
        String user = bundle.getString("dbUser");
        String pass = bundle.getString("dbPass");
        String url = bundle.getString("dbUrl");
        url = url + "?user=" + user + "&password=" + pass;
        source = new BasicDataSource();
        source.setDriverClassName(driver);
        source.setUsername(user);
        source.setPassword(pass);
        source.setUrl(url);
        source.setTestOnReturn(true);
        source.setValidationQuery("SELECT 1");
        logger.debug("connectionpool created");
        return source;
    }

    protected Connection getConnection() throws SQLException {
        if (connectionPool == null) {
            connectionPool = createConnectionPool();
        }
        return connectionPool.getConnection();

    }

    public void close() throws DaoException {
        try {
            if (connectionPool != null) {
                connectionPool.close();
                connectionPool = null;
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }
    }
}
