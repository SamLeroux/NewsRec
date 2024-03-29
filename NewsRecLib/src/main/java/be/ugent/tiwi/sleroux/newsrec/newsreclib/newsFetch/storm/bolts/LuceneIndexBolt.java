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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.LanguageAnalyzerHelper;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.NewsRecLuceneAnalyzer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.termExtract.LuceneTopTermExtract;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.NewsItemLuceneDocConverter;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
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
    private LuceneTopTermExtract termExtract;
    private static final Logger logger = Logger.getLogger(LuceneIndexBolt.class);

    /**
     *
     * @param indexLocation
     */
    public LuceneIndexBolt(String indexLocation) {
        this.indexLocation = indexLocation;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        try {
            logger.info("Opening index");
            Directory dir = FSDirectory.open(new File(indexLocation));
            NewsRecLuceneAnalyzer analyzer = LanguageAnalyzerHelper.getInstance().getAnalyzer(Locale.ENGLISH);
            this.termExtract = new LuceneTopTermExtract(analyzer);
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
            termExtract.setAnalyzer(LanguageAnalyzerHelper.getInstance().getAnalyzer(item.getLocale()));
            try (DirectoryReader reader = DirectoryReader.open(writer, true)) {
                termExtract.addTopTerms(item, reader);
            }

            // Convert to lucene document and add to index
            Document doc = NewsItemLuceneDocConverter.newsItemToDocument(item);
            writer.addDocument(doc);
            writer.commit();

            logger.info("emitting " + item.getTerms().size() + " terms");
            for (String term : item.getTerms().keySet()) {
                collector.emit(StreamIDs.TERMSTREAM, new Values(term));
            }

            logger.info("New item in Lucene index");

        } catch (IOException ex) {
            logger.error(ex);
        }
        collector.ack(input);

    }

}
