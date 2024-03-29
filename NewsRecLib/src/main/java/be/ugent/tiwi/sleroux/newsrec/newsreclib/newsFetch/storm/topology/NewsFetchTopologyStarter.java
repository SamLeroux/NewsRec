package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITwitterFollowersDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.StormException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.FetchArticleContentBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.LuceneIndexBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.RssFetchBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.UpdateNewsSourceBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.spouts.FeedSourceSpout;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.StormRunner;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.LoggingBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.TrendingTermsToDatabaseBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.TweetAnalyzerBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect.IntermediateRankingsBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect.RollingCountBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect.TotalRankingsBolt;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.spouts.GoogleTrendsSpout;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.spouts.TweetsSpout;

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
public class NewsFetchTopologyStarter {

    private final INewsSourceDao newsSourceDao;
    private final ITrendsDao trendsDao;
    private final ITwitterFollowersDao followersDao;
    private final String name;
    private final String luceneIndexLocation;
    private final int tickFreq = 60;
    private final int window = 60 * 60;
    private final int topN = 25;

    /**
     *
     * @param newsSourceDao
     * @param trendsDao
     * @param name
     * @param luceneIndexLocation
     */
    public NewsFetchTopologyStarter(INewsSourceDao newsSourceDao, ITrendsDao trendsDao, ITwitterFollowersDao followersDao, String name, String luceneIndexLocation) {
        this.newsSourceDao = newsSourceDao;
        this.trendsDao = trendsDao;
        this.followersDao = followersDao;
        this.name = name;
        this.luceneIndexLocation = luceneIndexLocation;
    }

    private StormTopology buildTopology() {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("feedurlspout", new FeedSourceSpout(newsSourceDao));
        builder.setSpout("trendsspout", new GoogleTrendsSpout());
        builder.setSpout("tweetsspout", new TweetsSpout(followersDao));

        builder.setBolt("tweetAnalyzerBolt", new TweetAnalyzerBolt())
                .fieldsGrouping("tweetsspout", StreamIDs.TWEETSTREAM, new Fields(StreamIDs.TWEET));

        builder.setBolt("loggingbolt", new LoggingBolt())
                .allGrouping("tweetAnalyzerBolt", StreamIDs.TERMSTREAM)
                .allGrouping("tweetsspout", StreamIDs.TWEETSTREAM);
        builder.setBolt("rssfetchbolt", new RssFetchBolt(), 1).globalGrouping("feedurlspout", StreamIDs.NEWSSOURCESTREAM);

        builder.setBolt("contentFetchBolt", new FetchArticleContentBolt(), 1)
                .globalGrouping("rssfetchbolt", StreamIDs.NEWSARTICLENOCONTENTSTREAM);
        builder.setBolt("updatesourcebolt", new UpdateNewsSourceBolt(newsSourceDao), 1)
                .globalGrouping("rssfetchbolt", StreamIDs.UPDATEDNEWSSOURCESTREAM);

        builder.setBolt("luceneIndexBolt", new LuceneIndexBolt(luceneIndexLocation), 1)
                .globalGrouping("contentFetchBolt", StreamIDs.NEWSARTICLEWITHCONTENTSTREAM);
        builder.setBolt("counterBolt", new RollingCountBolt(window, tickFreq), 4)
                .fieldsGrouping("luceneIndexBolt", StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM))
                .fieldsGrouping("trendsspout", StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM))
                .fieldsGrouping("tweetAnalyzerBolt", StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));

        builder.setBolt("intermediateRankerBolt", new IntermediateRankingsBolt(topN, tickFreq), 4)
                .fieldsGrouping("counterBolt", new Fields("obj"));
        builder.setBolt("rankerBolt", new TotalRankingsBolt(topN, tickFreq), 1)
                .globalGrouping("intermediateRankerBolt");

        builder.setBolt("databaseTermsBolt", new TrendingTermsToDatabaseBolt(trendsDao), 1)
                .globalGrouping("rankerBolt");

        return builder.createTopology();
    }

    private Config createTopologyConfiguration() {
        Config conf = new Config();
        conf.setDebug(false);
        conf.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 10000);
        conf.put(Config.NIMBUS_SUPERVISOR_TIMEOUT_SECS, 10000);
        conf.put(Config.SUPERVISOR_WORKER_TIMEOUT_SECS, 10000);
        conf.put(Config.STORM_CLUSTER_MODE, "distributed");
        conf.put(Config.STORM_ZOOKEEPER_CONNECTION_TIMEOUT, 10000);
        conf.put(Config.STORM_ZOOKEEPER_SESSION_TIMEOUT, 10000);
        conf.put(Config.TOPOLOGY_SLEEP_SPOUT_WAIT_STRATEGY_TIME_MS, 1000);
        return conf;
    }

    public void startLocal() throws StormException {
        Config config = createTopologyConfiguration();
        config.put(Config.STORM_CLUSTER_MODE, "local");
        StormTopology topology = buildTopology();
        StormRunner.runTopologyLocally(topology, name, config);
    }

    public void startOnCLuster() throws StormException {
        Config config = createTopologyConfiguration();
        StormTopology topology = buildTopology();
        StormRunner.runTopologyOnCLuster(topology, name, config);
    }

    /**
     *
     */
    public void stopLocal() {
        StormRunner.stop(name);
        StormRunner.shutdown();
    }
}
