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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
        declarer.declareStream(StreamIDs.INDEXEDITEMSTREAM, new Fields(StreamIDs.INDEXEDITEM));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
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
            writer.close();
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
    
    

    @Override
    public void execute(Tuple input) {
        collector.ack(input);
        try {
            logger.info("New item to add to lucene index");
            NewsItem item = (NewsItem) input.getValueByField(StreamIDs.NEWSARTICLEWITHCONTENT);
            Document doc = newsItemToDoc(item);
            writer.addDocument(doc);
            writer.commit();
            collector.emit(StreamIDs.INDEXEDITEMSTREAM,new Values(item));
            logger.info("New item in Lucene index");
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        
        
    }

    private FieldType getType() {
        FieldType ftype = new FieldType();
        ftype.setIndexed(true);
        ftype.setStoreTermVectors(true);
        ftype.setStored(true);
        return ftype;
    }

    private Document newsItemToDoc(NewsItem item) {
        logger.info("converting item to document");
        
        Document doc = new Document();
        FieldType ftype = getType();
        
        if (item.getTitle() != null) {
            doc.add(new Field("title", item.getTitle(), ftype));
        }
        if (item.getFulltext() != null) {
            doc.add(new Field("text", item.getFulltext(), ftype));
        }
        if (item.getDescription() != null) {
            doc.add(new Field("description", item.getDescription(), ftype));
        }

        doc.add(new LongField("id", item.getId(), Field.Store.YES));

        if (item.getUrl() != null) {
            doc.add(new StringField("url", item.getUrl().toString(), Field.Store.YES));
        }
        if (item.getImageUrl() != null) {
            doc.add(new StringField("imageUrl", item.getImageUrl().toString(), Field.Store.YES));
        }
        if (item.getLocale() != null) {
            doc.add(new StringField("locale", item.getLocale().getISO3Language(), Field.Store.YES));
        }
        if (item.getSource() != null) {
            doc.add(new StringField("source", item.getSource(), Field.Store.YES));
        }
        if (item.getTimestamp() != null) {
            doc.add(new LongField("timestamp", item.getTimestamp().getTime(), Field.Store.YES));
        }

        for (String author : item.getAuthors()) {
            doc.add(new StringField("author", author, Field.Store.YES));
        }

        Map<String, Float> terms = item.getTerms();
        for (String term : terms.keySet()) {
            TextField tf = new TextField("term", term, Field.Store.YES);
            tf.setBoost(terms.get(term));
            doc.add(tf);
        }
        return doc;
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
