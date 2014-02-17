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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Timer to automatically check for new articles. When a new article has been
 * found, it will inform all registered listeners
 *
 * @see INewsItemListener
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsFetchTimer {

    private INewsSourceDao dao;
    private INewsFetcher newsfetcher;
    private int checkInterval;
    private final Timer timer;
    private final List<INewsItemListener> listeners;

    /**
     * 
     * @param dao The NewsSourceDao to use.
     * @param newsfetcher The Newsfetcher to use
     * @param checkInterval The time in milliseconds between checks.
     * @see INewsSourceDao
     * @see INewsFetcher
     */
    public NewsFetchTimer(INewsSourceDao dao, INewsFetcher newsfetcher, int checkInterval) {
        this.dao = dao;
        this.newsfetcher = newsfetcher;
        this.checkInterval = checkInterval;
        timer = new Timer();
        listeners = new ArrayList<>();

    }

    public INewsSourceDao getDao() {
        return dao;
    }

    public void setDao(INewsSourceDao dao) {
        this.dao = dao;
    }

    public INewsFetcher getNewsfetcher() {
        return newsfetcher;
    }

    public void setNewsfetcher(INewsFetcher newsfetcher) {
        this.newsfetcher = newsfetcher;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    /**
     * Start polling
     */
    public void start() {
        timer.scheduleAtFixedRate(new FetchTask(), 0, checkInterval);
    }

    /**
     * Stop polling
     */
    public void stop() {
        timer.cancel();
    }

    public void addListener(INewsItemListener listener) {
        listeners.add(listener);
    }

    private class FetchTask extends TimerTask {

        @Override
        public void run() {
            try {
                dao.startSession();
                NewsSource[] sources = dao.getSourcesToCheck();
                System.out.println(sources.length+" to check");
                for (NewsSource source : sources) {
                    try {
                        NewsItem[] items = newsfetcher.fetch(source);
                        
                        for (INewsItemListener listener : listeners) {
                            listener.newItem(items);
                        }
                    } catch (NewsFetchException ex) {
                        Logger.getLogger(NewsFetchTimer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                dao.stopSession();
            } catch (DaoException ex) {
                Logger.getLogger(NewsFetchTimer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
