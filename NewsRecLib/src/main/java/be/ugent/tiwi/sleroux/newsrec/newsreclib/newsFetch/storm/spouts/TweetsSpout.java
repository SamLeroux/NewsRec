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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITwitterFollowersDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.twitter.StatusListenerAdapter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.twitter.TwitterStreamBuilder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.TwitterStream;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TweetsSpout extends BaseRichSpout {

    private LinkedBlockingQueue<String> termsQueue;
    private SpoutOutputCollector collector;
    private TwitterStream stream;

    private final ITwitterFollowersDao followersDao;
    private static final Logger logger = Logger.getLogger(TweetsSpout.class);
    private static final Random rnd = new Random();

    public TweetsSpout(ITwitterFollowersDao followersDao) {
        this.followersDao = followersDao;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.TWEETSTREAM, new Fields(StreamIDs.TWEET));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        termsQueue = new LinkedBlockingQueue<>();
        this.collector = collector;
        startListener();
    }

    @Override
    public void nextTuple() {
        String term = termsQueue.poll();
        if (term != null) {
            collector.emit(StreamIDs.TWEETSTREAM, new Values(term));
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public void close() {
        super.close();
        stream.shutdown();
    }

    private void startListener() {
        try {
            stream = TwitterStreamBuilder.getStream();
            stream.addListener(new StatusListener());
            FilterQuery f = getFilterQuery();
            stream.filter(f);
        } catch (DaoException ex) {
            logger.error(ex);
        }
    }

    private FilterQuery getFilterQuery() throws DaoException {
        FilterQuery f = new FilterQuery();
        List<Long> users = followersDao.getUsersToFollow();
        long[] usersArray = new long[users.size()];
        int i = 0;
        for (Long l : users) {
            usersArray[i] = l;
            i++;
        }
        f.follow(usersArray);
        return f;
    }

    private class StatusListener extends StatusListenerAdapter {

        @Override
        public void onStatus(Status status) {
            if (!status.isRetweet() || (status.isRetweet() && rnd.nextInt(10) == 0)) {
                String text = status.getText();
                text = text.replaceAll("http://.*?($| )", "").replaceAll("#.*? ", "").replaceAll("@.*? ", "").replaceFirst("RT ", "");
                termsQueue.add(text);
            }
        }
    }

}
