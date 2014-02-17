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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DummyNewsSourceDao implements INewsSourceDao{

    private List<NewsSource> newsSources;

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
    public NewsSource[] getSourcesToCheck() {
        return getAllSources();
    }

    @Override
    public NewsSource[] getAllSources() {
        return newsSources.toArray(new NewsSource[newsSources.size()]);
    }

    @Override
    public void AddNewsSource(NewsSource source) {
        newsSources.add(source);
    }

    @Override
    public void close() {
    }
    
}
