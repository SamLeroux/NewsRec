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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts.trendDetect;

import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract bolt provides the basic behavior of bolts that rank objects
 * according to their count.
 * <p/>
 * It uses a template method design pattern for
 * {@link AbstractRankerBolt#execute(Tuple, BasicOutputCollector)} to allow
 * actual bolt implementations to specify how incoming tuples are processed,
 * i.e. how the objects embedded within those tuples are retrieved and counted.
 */
public abstract class AbstractRankerBolt extends BaseRichBolt {

    private static final long serialVersionUID = 4931640198501530202L;

    private final int emitFrequencyInSeconds;
    private final int count;
    private final Rankings rankings;
    private OutputCollector collector;

    /**
     *
     * @param topN
     * @param emitFrequencyInSeconds
     */
    public AbstractRankerBolt(int topN, int emitFrequencyInSeconds) {
        if (topN < 1) {
            throw new IllegalArgumentException("topN must be >= 1 (you requested " + topN + ")");
        }
        if (emitFrequencyInSeconds < 1) {
            throw new IllegalArgumentException(
                    "The emit frequency must be >= 1 seconds (you requested " + emitFrequencyInSeconds + " seconds)");
        }
        count = topN;
        this.emitFrequencyInSeconds = emitFrequencyInSeconds;
        rankings = new Rankings(count);
    }

    /**
     *
     * @return
     */
    protected Rankings getRankings() {
        return rankings;
    }

    @Override
    public final void execute(Tuple tuple) {
        if (TupleHelpers.isTickTuple(tuple)) {
            getLogger().debug("Received tick tuple, triggering emit of current rankings");
            collector.emit(new Values(rankings.copy()));
        } else {
            updateRankingsWithTuple(tuple);
        }
        collector.ack(tuple);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    abstract void updateRankingsWithTuple(Tuple tuple);

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("rankings"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> conf = new HashMap<>();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSeconds);
        return conf;
    }

    abstract Logger getLogger();
}
