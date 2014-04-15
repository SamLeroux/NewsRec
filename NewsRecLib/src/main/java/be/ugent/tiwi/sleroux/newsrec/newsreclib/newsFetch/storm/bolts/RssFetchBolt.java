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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.HashCircularBuffer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import com.sun.syndication.feed.synd.SyndEnclosure;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jdom.Element;

/**
 * Checks a news source for new articles.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RssFetchBolt extends BaseRichBolt {

    private HashCircularBuffer<String> articlesurlBuffer;
    private transient OutputCollector collector;
    private static final Logger logger = Logger.getLogger(RssFetchBolt.class);

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.NEWSARTICLENOCONTENTSTREAM, new Fields(StreamIDs.NEWSARTICLENOCONTENT, StreamIDs.NEWSARTICLESOURCE));
        declarer.declareStream(StreamIDs.UPDATEDNEWSSOURCESTREAM, new Fields(StreamIDs.UPDATEDNEWSSOURCE));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        articlesurlBuffer = new HashCircularBuffer<>(2500);
    }

    @Override
    public void execute(Tuple input) {

        NewsSource source = (NewsSource) input.getValueByField(StreamIDs.NEWSSOURCEITEM);
        logger.info("checking: " + source.getName());
        try {
            source.setLastFetchTry(new Date());

            URL url = source.getRssUrl();
            HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
            httpcon.setConnectTimeout(15000);
            httpcon.setReadTimeout(15000);

            SyndFeedInput feedinput = new SyndFeedInput();
            SyndFeed feed = feedinput.build(new XmlReader(httpcon));
            List<SyndEntry> entries = feed.getEntries();

            // update news source information
            source.setDescription(feed.getDescription());
            source.setName(feed.getTitle());

            // The timestamp of the article that was last fetched.
            Date lastSeen = null;
            int count = 0;
            int i = 0;
            while (i < entries.size()
                    && (source.getLastArticleFetchTime() == null
                    || (entries.get(i).getPublishedDate() != null
                    && entries.get(i).getPublishedDate().after(source.getLastArticleFetchTime())))) {

                SyndEntry entry = entries.get(i);
                count++;
                if (!articlesurlBuffer.contains(entry.getTitle())) {
                    NewsItem item = generateNewsItem(source, entry);

                    // Store the timestamp to make sure we don't process this article
                    // again next time.
                    if (lastSeen == null || entry.getPublishedDate().after(lastSeen)) {
                        lastSeen = entry.getPublishedDate();
                    }

                    articlesurlBuffer.putNoCheck(entry.getTitle());

                    collector.emit(StreamIDs.NEWSARTICLENOCONTENTSTREAM, new Values(item, item.getSource()));

                } else {
                    logger.info("Skipping: article, article with same title has already been added (title=" + entry.getTitle() + ")");
                }
                i++;
            }

            if (lastSeen != null) {
                source.setLastArticleFetchTime(lastSeen);
            }

            // Exponential backoff
            // When there were no new articles, increase the interval, otherwise 
            // decrease the interval. Do not go below 30 seconds.
            logger.info("current fetchinterval: " + source.getFetchinterval());
            if (count == 0) {
                source.setFetchinterval(source.getFetchinterval() * 2);
            } else {
                int interval = source.getFetchinterval() / 2;
                interval = (interval < 300 ? 300 : interval);
                source.setFetchinterval(interval);
            }
            logger.info("found " + count + " new articles");
            logger.info("New fetchinterval " + source.getFetchinterval());

        } catch (MalformedURLException ex) {
            source.setFetchinterval(source.getFetchinterval() * 4);
            logger.error("Invalid url", ex);
        } catch (IOException ex) {
            source.setFetchinterval(source.getFetchinterval() * 4);
            logger.error("IOexception", ex);
        } catch (IllegalArgumentException | FeedException ex) {
            source.setFetchinterval(source.getFetchinterval() * 4);
            logger.error(ex.getMessage(), ex);
        }
        collector.ack(input);
        collector.emit(StreamIDs.UPDATEDNEWSSOURCESTREAM, new Values(source));

    }

    private NewsItem generateNewsItem(NewsSource source, SyndEntry entry) throws MalformedURLException {
        logger.info("Generating news item from feed entry");
        NewsItem item = new NewsItem();
        item.setTitle(Jsoup.parse(entry.getTitle()).text());

        for (Object o : entry.getAuthors()) {
            SyndPerson p = (SyndPerson) o;
            item.addAuthor(p.getName());
        }

        item.setTimestamp(entry.getPublishedDate());

        if (entry.getDescription() != null) {
            Document doc = Jsoup.parse(entry.getDescription().getValue());
            item.setDescription(doc.text());
        } else {
            item.setDescription("No description available.");
        }
        item.setUrl(new URL(entry.getLink()));
        item.setSource(source.getName());
        item.setImageUrl(findImage(source, entry));
//        for (Object o : entry.getCategories()) {
//            SyndCategory cat = (SyndCategory) o;
//            item.addTerm(cat.getName(), 0.75F);
//        }

        return item;
    }

    private URL findImage(NewsSource source, SyndEntry entry) {
        String image = null;
        List<SyndEnclosure> encls = entry.getEnclosures();

        if (!encls.isEmpty()) {
            SyndEnclosure e = encls.get(0);
            image = e.getUrl();
        }

        if (image == null) {
            List<Element> foreignMarkups = (List<Element>) entry.getForeignMarkup();
            if (!foreignMarkups.isEmpty()) {
                Element e = foreignMarkups.get(0);
                Attribute a = e.getAttribute("url");
                if (a != null) {
                    image = a.getValue();
                }
            }
        }

        if (image != null && image.startsWith("/")) {
            String prefix = source.getRssUrl().getHost();
            image = prefix + image;
        }

        URL result = null;
        if (image != null) {
            try {
                result = new URL(image);
            } catch (MalformedURLException ex) {
            }
        }
        return result;
    }
}
