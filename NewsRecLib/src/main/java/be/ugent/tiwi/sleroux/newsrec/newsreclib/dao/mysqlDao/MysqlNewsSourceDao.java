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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlDao;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class MysqlNewsSourceDao extends HibernateDaoTemplate implements INewsSourceDao {

    @Override
    public NewsSource[] getSourcesToCheck() {
        
        Session session = getSession();
        Query query = session.createQuery("from NewsSource where (lastFetchTry + fetchinterval < CURRENT_TIMESTAMP()) or lastFetchTry = null");
        List<NewsSource> sources = query.list();
        
        return sources.toArray(new NewsSource[sources.size()]);
    }

    @Override
    public NewsSource[] getAllSources() {
        Session session  = getSession();
        Query query = session.createQuery("from NewsSource");
        List<NewsSource> sources = query.list();
        return sources.toArray(new NewsSource[sources.size()]);
    }

    @Override
    public void AddNewsSource(NewsSource source) {
        Session session  = getSession();
        Transaction t = session.beginTransaction();
        session.saveOrUpdate(source);
        t.commit();
        closeSession();
    }

    @Override
    public void close() {
        closeSession();
    }

    
}
