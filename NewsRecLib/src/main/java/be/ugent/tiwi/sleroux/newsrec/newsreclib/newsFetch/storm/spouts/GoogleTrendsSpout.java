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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class GoogleTrendsSpout extends BaseRichSpout {

    private SpoutOutputCollector collector;
    private LinkedBlockingQueue<String> termsQueue;
    private Timer timer;
    private static final Logger logger = Logger.getLogger(FeedSourceSpout.class);
    private static final ResourceBundle bundle = PropertyResourceBundle.getBundle("newsRec");
    private String trendsUrl;
    private static final String REGEX = "<li><span class=\".*?\"><a href=\".*?\">(.*?)</a></span></li>";
    private static final Random rnd = new Random();

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));

    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        trendsUrl = bundle.getString("trendsUrl");
        termsQueue = new LinkedBlockingQueue<>();
        timer = new Timer();
        timer.schedule(new FetchTask(), 0, 1000 * 60 * 30);

    }

    @Override
    public void nextTuple() {
        if (rnd.nextInt(50) == 0) {
            String term = termsQueue.poll();
            if (term != null) {
                collector.emit(StreamIDs.TERMSTREAM, new Values(term));
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    private class FetchTask extends TimerTask {

        @Override
        public void run() {
            BufferedInputStream in = null;
            try {
                URL url = new URL(trendsUrl);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                HttpURLConnection.setFollowRedirects(true);
                urlConnection.setRequestProperty("User-Agent", bundle.getString("useragent"));
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
                in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] content = IOUtils.toByteArray(in);
                in.close();
                urlConnection.disconnect();

                String html = new String(content);

                Matcher m = Pattern.compile(REGEX).matcher(html);
                while (m.find()) {
                    String term = m.group(1);
                    for (int i = 0; i < 100; i++) {
                        termsQueue.add(term);
                    }
                }

            } catch (MalformedURLException ex) {
                logger.error(ex);
            } catch (IOException ex) {
                logger.error(ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

}
