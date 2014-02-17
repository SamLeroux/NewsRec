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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance.EnhanceException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.HttpURLConnection;
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
public class RssNewsFetcher extends AbstractNewsfetcher {

    @Override
    public NewsItem[] fetch(NewsSource source) throws NewsFetchException {
        List<NewsItem> items = new ArrayList<>();
        try {
            URL url = source.getRssUrl();
            HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(httpcon));
            List<SyndEntry> entries = feed.getEntries();

            // update news source information
            source.setDescription(feed.getDescription());
            source.setName(feed.getTitle());
            source.setLastFetchTry(new Date());
            

            // The timestamp of the article that was last fetched.
            Date lastSeen = null;

            for (SyndEntry entry : entries) {
                // If this article has not been seen before
                if (source.getLastArticleFetchTime() == null || entry.getPublishedDate().after(source.getLastArticleFetchTime())) {

                    NewsItem item = new NewsItem();
                    item.setTitle(entry.getTitle());

                    for (Object o : entry.getAuthors()) {
                        SyndPerson p = (SyndPerson) o;
                        item.addAuthor(p.getName());
                    }

                    item.setTimestamp(entry.getPublishedDate());

                    item.setDescription(entry.getDescription().getValue());
                    item.setUrl(new URL(entry.getLink()));
                    item.setSource(source);

                    for (Object o : entry.getCategories()) {
                        SyndCategory cat = (SyndCategory) o;
                        item.addTerm(cat.getName(), 0.75);
                    }

                    // Pass the article to the enhancement chain.
                    enhance(item);
                    
                    items.add(item);

                    // Store the timestamp to make sure we don't process this article
                    // again next time.
                    if (lastSeen == null || entry.getPublishedDate().after(lastSeen)) {
                        lastSeen = entry.getPublishedDate();
                    }
                }
            }
            if (lastSeen != null) {
                source.setLastArticleFetchTime(lastSeen);
            }
            
            // Exponential backoff
            // When there were no new articles, increase the interval, otherwise 
            // decrease the interval. Do not go below 30 seconds.
            if(items.isEmpty()){
                source.setFetchinterval(source.getFetchinterval()*2);
            }
            else{
                int interval = source.getFetchinterval()/2;
                interval = (interval < 30 ? 30: interval);
                source.setFetchinterval(interval);
            }
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException | FeedException | EnhanceException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        return items.toArray(new NewsItem[items.size()]);
    }

}
