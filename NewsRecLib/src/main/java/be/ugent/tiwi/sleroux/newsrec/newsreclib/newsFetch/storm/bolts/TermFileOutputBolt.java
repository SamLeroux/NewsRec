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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Writes all incoming terms to a text file.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TermFileOutputBolt extends BaseRichBolt {

    private BufferedWriter writer;
    private OutputCollector collector;
    private static final Logger logger = Logger.getLogger(TermFileOutputBolt.class);

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public void cleanup() {
        super.cleanup();
        try {
            writer.close();
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        try {
            logger.info("Open term output file");
            writer = new BufferedWriter(new FileWriter("/home/sam/Bureaublad/terms.txt", true));
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void execute(Tuple input) {
        Fields fields = input.getFields();
        for (String field : fields) {
            try {
                writer.write(input.getValueByField(field).toString());
                writer.newLine();
                writer.write("-------------------------------------------------------");
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        try {
            writer.write("-------------------------------------------------------");
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            logger.error(ex);
        }
        collector.ack(input);
    }

}
