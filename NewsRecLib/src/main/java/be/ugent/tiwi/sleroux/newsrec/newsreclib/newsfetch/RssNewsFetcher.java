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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RssNewsFetcher extends AbstractNewsfetcher{

    public NewsItem[] fetch(NewsSource source) throws NewsFetchException {
        List<NewsItem> items = new ArrayList<>();
        try {
            URL url = new URL(source.getRssUrl());
            HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(httpcon));
            List<SyndEntry> entries = feed.getEntries();

            for (SyndEntry entry : entries) {
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
                
                enhance(item);
                
                items.add(item);
                System.out.println(item);
                break;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FeedException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EnhanceException ex) {
            Logger.getLogger(RssNewsFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return items.toArray(new NewsItem[items.size()]);
    }

    
}
