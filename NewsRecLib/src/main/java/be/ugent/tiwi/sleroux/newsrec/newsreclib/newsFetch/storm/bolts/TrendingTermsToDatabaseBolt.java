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
import backtype.storm.tuple.Tuple;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect.Rankable;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect.RankableObjectWithFields;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect.Rankings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TrendingTermsToDatabaseBolt extends BaseRichBolt {

    private OutputCollector collector;
    private final ITrendsDao trendsDao;
    private static final Logger logger = Logger.getLogger(TrendingTermsToDatabaseBolt.class);

    public TrendingTermsToDatabaseBolt(ITrendsDao trendsDao) {
        this.trendsDao = trendsDao;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        try {
            List<String> terms = new ArrayList<>();
            Rankings rankings = (Rankings) input.getValueByField("rankings");
            List<Rankable> rl = rankings.getRankings();
            for (Rankable r : rl) {
                RankableObjectWithFields rowf = (RankableObjectWithFields) r;
                terms.add(rowf.getObject().toString());
            }
            if (!terms.isEmpty()) {
                logger.info("Storing " + terms.size() + " trending terms in database");
                trendsDao.updateTrends(terms.toArray(new String[terms.size()]));
            }
        } catch (TrendsDaoException ex) {
            logger.error(ex);
        }
        collector.ack(input);
    }

}
