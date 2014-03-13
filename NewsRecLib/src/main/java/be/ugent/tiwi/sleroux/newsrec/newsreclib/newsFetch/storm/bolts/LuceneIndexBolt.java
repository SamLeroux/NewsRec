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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.config.Config;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.EnAnalyzer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.termExtract.LuceneTopTermExtract;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.NewsItemLuceneDocConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LuceneIndexBolt extends BaseRichBolt {

    private IndexWriter writer;
    private OutputCollector collector;
    private final String indexLocation;
    private final String stopwordsLocation;
    private LuceneTopTermExtract termExtract;
    private static final Logger logger = Logger.getLogger(LuceneIndexBolt.class);

    /**
     *
     * @param indexLocation
     * @param stopwordsLocation
     */
    public LuceneIndexBolt(String indexLocation, String stopwordsLocation) {
        this.indexLocation = indexLocation;
        this.stopwordsLocation = stopwordsLocation;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.termExtract = new LuceneTopTermExtract();
        try {
            logger.info("Opening index");
            Directory dir = FSDirectory.open(new File(indexLocation));
            EnAnalyzer analyzer = new EnAnalyzer();
            analyzer.setStopwords(getStopwords(stopwordsLocation));
            IndexWriterConfig config = new IndexWriterConfig(Config.LUCENE_VERSION, analyzer);
            writer = new IndexWriter(dir, config);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void execute(Tuple input) {
        try {
            logger.info("New item to add to lucene index");
            
            // input newsitem
            NewsItem item = (NewsItem) input.getValueByField(StreamIDs.NEWSARTICLEWITHCONTENT);

            // Convert to lucene document and add to index
            Document doc = NewsItemLuceneDocConverter.NewsItemToDocument(item);
            writer.addDocument(doc);
            writer.commit();
            
            // fetch the important terms from this document and pass them on to the
            // next bolt.
            try (IndexReader reader = DirectoryReader.open(writer, true)) {
                Map<String, Double> terms = termExtract.getTopTerms(item.getId(), reader);

                logger.info("emitting " + terms.size() + " terms");
                for (String term : terms.keySet()) {
                    collector.emit(StreamIDs.TERMSTREAM, new Values(term));
                }

                logger.info("New item in Lucene index");
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
        collector.ack(input);

    }

    private CharArraySet getStopwords(String stopwordsLocation) throws FileNotFoundException, IOException {
        logger.info("reading stopwords file: " + stopwordsLocation);
        CharArraySet stopw;
        try (BufferedReader reader = new BufferedReader(new FileReader(stopwordsLocation))) {
            stopw = new CharArraySet(Config.LUCENE_VERSION, 1000, true);
            String line = reader.readLine();
            while (line != null) {
                stopw.add(line);
                line = reader.readLine();
            }
        }
        return stopw;
    }

}
