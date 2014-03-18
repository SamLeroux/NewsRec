package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

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




/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class FeedSourceSpout extends BaseRichSpout {

    private LinkedBlockingQueue<NewsSource> sourcesQueue;
    private SpoutOutputCollector collector;
    private Timer timer;
    private final INewsSourceDao newsSourceDao;
    private static final Logger logger = Logger.getLogger(FeedSourceSpout.class);

    /**
     *
     * @param newsSourceDao
     */
    public FeedSourceSpout(INewsSourceDao newsSourceDao) {
        this.newsSourceDao = newsSourceDao;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.NEWSSOURCESTREAM,new Fields(StreamIDs.NEWSSOURCEITEM));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        sourcesQueue = new LinkedBlockingQueue<>();
        timer = new Timer();
        timer.schedule(new FetchTask(), 0, 60000);
        this.collector = collector;
    }

    @Override
    public void close() {
        timer.cancel();
        super.close();         
    }

    
    @Override
    public void nextTuple() {
        NewsSource source = sourcesQueue.poll();
        if (source != null) {
            collector.emit(StreamIDs.NEWSSOURCESTREAM,new Values(source));
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                logger.error(ex);
            }
        }
    }

    private class FetchTask extends TimerTask {

        @Override
        public void run() {
            try {
                NewsSource[] sources = newsSourceDao.getSourcesToCheck();
                sourcesQueue.addAll(Arrays.asList(sources));
                logger.info(sources.length+" feeds to check");
            } catch (DaoException ex) {
                logger.error(ex);
            }

        }

    }

}
