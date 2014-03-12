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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.TermScorePair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

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
        declarer.declareStream(StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));
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
        try {
            logger.info("New item to add to lucene index");
            NewsItem item = (NewsItem) input.getValueByField(StreamIDs.NEWSARTICLEWITHCONTENT);
            Document doc = newsItemToDoc(item);
            writer.addDocument(doc);
            updateDocterms(item);
            writer.commit();
            logger.info("emitting terms");
            for (String term : item.getTerms().keySet()) {
                collector.emit(StreamIDs.TERMSTREAM, new Values(term));
            }
            logger.info("New item in Lucene index");
        } catch (IOException ex) {
            logger.error(ex);
        }
        collector.ack(input);

    }

    private FieldType getTextType() {
        FieldType ftype = new FieldType();
        ftype.setIndexed(true);
        ftype.setStoreTermVectors(true);
        ftype.setStored(true);
        ftype.freeze();
        return ftype;
    }

    private Document newsItemToDoc(NewsItem item) {
        logger.info("converting item to document");

        Document doc = new Document();
        FieldType ftype = getTextType();

        if (item.getTitle() != null) {
            doc.add(new Field("title", item.getTitle(), ftype));
        }
        if (item.getFulltext() != null) {
            doc.add(new Field("text", item.getFulltext(), ftype));
        }
        if (item.getDescription() != null) {
            doc.add(new Field("description", item.getDescription(), ftype));
        }

        
        doc.add(new StringField("id", item.getId(), Field.Store.YES));

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

    private void updateDocterms(NewsItem item) throws IOException {
        IndexReader reader = DirectoryReader.open(writer, true);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query q = new TermQuery(new Term("id", item.getId()));
        TopScoreDocCollector col = TopScoreDocCollector.create(1, true);
        searcher.search(q, col);
        ScoreDoc[] hits = col.topDocs().scoreDocs;
        if (hits.length == 1) {
            int docnr = hits[0].doc;
            Document doc = reader.document(docnr);
            Map<String, Double> terms = getTopterms(docnr, reader);
            for (String term : terms.keySet()) {
                item.addTerm(term, terms.get(term).floatValue());
                doc.add(new StringField("term", term, Field.Store.YES));
            }
            if (!writer.tryDeleteDocument(reader, docnr)) {
                logger.warn("could not delete document with docnr=" + docnr);
            }
            writer.addDocument(doc);
        } else {
            logger.error("Hits.length should be 1, was " + hits.length);
        }

    }

    public Map<String, Double> getTopterms(int item, IndexReader reader) {
        // fetch the terms occuring in this document
        Map<String, Double> termMap = new HashMap<>(250);
        updateTermMap(termMap, item, "text", 1, reader);
        // terms in the title adn description are more important than terms in the text.
        updateTermMap(termMap, item, "description", 1.5, reader);
        updateTermMap(termMap, item, "title", 2, reader);

        if (termMap.size() > 0) {
            // Only store the n most important terms.
            PriorityQueue<TermScorePair> pq = new PriorityQueue<>(termMap.size());
            double avg = 0;
            for (double d : termMap.values()) {
                avg += d;
            }
            avg /= termMap.size();

            for (String term : termMap.keySet()) {
                if (termMap.get(term) > avg) {
                    TermScorePair p = new TermScorePair(term, termMap.get(term));
                    pq.add(p);
                }

            }
            int n = (pq.size() < 10 ? pq.size() : 10);
            int i = 0;
            TermScorePair tsp = pq.poll();
            Map<String, Double> termsToStore = new HashMap<>();
            while (i < n && tsp != null) {
                termsToStore.put(tsp.getTerm(), tsp.getScore());
                tsp = pq.poll();
                i++;
            }
            return termsToStore;
        }
        return new HashMap<>();
    }

    /**
     * Fetch the document and copy all terms and term frequencies to the map.
     * Multiply the weight of each term by the provided weight factor.
     *
     * @param termMap
     * @param item
     * @param field
     * @param weight
     */
    private void updateTermMap(Map<String, Double> termMap, int item, String field, double weight, IndexReader reader) {

        try {

            Terms vector = reader.getTermVector(item, field);
            if (vector != null) {
                TermsEnum termsEnum;
                termsEnum = vector.iterator(TermsEnum.EMPTY);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    String term = text.utf8ToString();
                    int docFreq = reader.docFreq(new Term(field, text));
                    // ignore really rare terms and really common terms
                    double minFreq = reader.numDocs() * 0.0001;
                    double maxFreq = reader.numDocs() / 3;
                    if (docFreq > minFreq && docFreq < maxFreq) {
                        double tf = 1 + ((double) termsEnum.totalTermFreq()) / reader.getSumTotalTermFreq(field);
                        double idf = Math.log((double) reader.numDocs() / docFreq);
                        if (!Double.isInfinite(idf)) {
                            if (!termMap.containsKey(term)) {
                                termMap.put(term, tf * idf * weight);
                            } else {
                                termMap.put(term, termMap.get(term) + tf * idf * weight);
                            }
                        }
                    }
                }
            }
        } catch (IOException | NullPointerException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

}
