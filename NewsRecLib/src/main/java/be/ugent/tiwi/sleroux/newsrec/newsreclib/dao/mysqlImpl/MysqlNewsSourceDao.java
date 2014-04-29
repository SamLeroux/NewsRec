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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import java.io.Serializable;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Query;

/**
 * MySQL implementation of INewsSourceDao.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class MysqlNewsSourceDao extends HibernateDaoTemplate implements INewsSourceDao, Serializable {

    private static final Logger logger = Logger.getLogger(MysqlNewsSourceDao.class);

    @Override
    public NewsSource[] getSourcesToCheck() throws DaoException {
        try {
            startSession();
            Query query = session.createQuery("from NewsSource where lastFetchTry = null or (lastFetchTry + fetchinterval < CURRENT_TIMESTAMP())");
            List<NewsSource> sources = query.list();
            stopSession();
            return sources.toArray(new NewsSource[sources.size()]);
        } catch (Exception ex) {
            logger.error(ex);
            return new NewsSource[0];
        }
    }

    /**
     *
     * @return @throws DaoException
     */
    @Override
    public NewsSource[] getAllSources() throws DaoException {
        try {
            startSession();
            Query query = session.createQuery("from NewsSource");
            List<NewsSource> sources = query.list();
            stopSession();
            return sources.toArray(new NewsSource[sources.size()]);
        } catch (Exception ex) {
            logger.error(ex);
            return new NewsSource[0];
        }

    }

    /**
     *
     * @param source
     * @throws DaoException
     */
    @Override
    public void addNewsSource(NewsSource source) throws DaoException {
        startSession();
        session.saveOrUpdate(source);
        stopSession();
    }

    /**
     *
     * @param source
     * @throws DaoException
     */
    @Override
    public void updateNewsSource(NewsSource source) throws DaoException {
        startSession();
        session.update(source);
        stopSession();
    }

}
