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

package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.storm.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.model.NewsSource;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.storm.topology.StreamIDs;
import java.util.Map;
import org.apache.log4j.Logger;
/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class UpdateNewsSourceBolt extends BaseRichBolt{

    private final INewsSourceDao newsSourceDao;
    private OutputCollector collector;
    private static final Logger logger = Logger.getLogger(UpdateNewsSourceBolt.class);

    /**
     *
     * @param newsSourceDao
     */
    public UpdateNewsSourceBolt(INewsSourceDao newsSourceDao) {
        this.newsSourceDao = newsSourceDao;
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
            NewsSource source = (NewsSource)input.getValueByField(StreamIDs.UPDATEDNEWSSOURCE);
            newsSourceDao.updateNewsSource(source);
        } catch (DaoException ex) {
            logger.error(ex);
        }
        collector.ack(input);
    }
    
}
