package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.mysqlImpl;

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



import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.model.NewsSource;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DebuggingMysqlNewsSourceDao extends HibernateDaoTemplate implements INewsSourceDao{
    @Override
    public NewsSource[] getSourcesToCheck() {
        Query query = session.createQuery("from NewsSource where id=410");
        List<NewsSource> sources = query.list();

        return sources.toArray(new NewsSource[sources.size()]);
    }

    @Override
    public NewsSource[] getAllSources() {
        Query query = session.createQuery("from NewsSource");
        List<NewsSource> sources = query.list();
        return sources.toArray(new NewsSource[sources.size()]);
    }

    @Override
    public void AddNewsSource(NewsSource source) {
        session.saveOrUpdate(source);
    }

    @Override
    public void updateNewsSource(NewsSource source) throws DaoException {
        session.update(source);
    }
}
