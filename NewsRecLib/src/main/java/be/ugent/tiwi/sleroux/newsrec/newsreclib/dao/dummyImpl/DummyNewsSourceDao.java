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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.dummyImpl;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DummyNewsSourceDao implements INewsSourceDao {

    private List<NewsSource> newsSources;
    private boolean started = false;

    public DummyNewsSourceDao() {
        try {
            newsSources = new ArrayList<>();
            NewsSource n = new NewsSource();
            n.setName("CNN - Top Stories");
            n.setRssUrl(new URL("http://rss.cnn.com/rss/edition.rss"));
            n.setFetchinterval(5);
            newsSources.add(n);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DummyNewsSourceDao.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public NewsSource[] getSourcesToCheck() throws DaoException {
        if (!started){
            throw new DaoException("Session not started");
        }
        return getAllSources();
    }

    @Override
    public NewsSource[] getAllSources() throws DaoException {
        if (!started){
            throw new DaoException("Session not started");
        }
        return newsSources.toArray(new NewsSource[newsSources.size()]);
    }

    @Override
    public void addNewsSource(NewsSource source) throws DaoException {
        if (!started){
            throw new DaoException("Session not started");
        }
        newsSources.add(source);
    }

    @Override
    public void startSession() throws DaoException {
        if (started){
            throw new DaoException("Previous session not closed");
        }
        started = true;
    }

    @Override
    public void stopSession() throws DaoException {
        if (!started){
            throw new DaoException("Session not started");
        }
        started = false;
    }

    @Override
    public void updateNewsSource(NewsSource source) throws DaoException {
    }

}
