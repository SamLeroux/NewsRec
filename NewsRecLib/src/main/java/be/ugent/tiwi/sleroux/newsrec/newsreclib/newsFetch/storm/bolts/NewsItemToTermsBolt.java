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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.TermScorePair;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * After the news item is indexed in Lucene, the information about term
 * frequency and document frequency is available. This bolt extracts the top 10
 * terms from the newsitem and makes them available for the next bolt.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
@Deprecated
public class NewsItemToTermsBolt extends BaseRichBolt {

    private SearcherManager searcherManager;
    private ReaderManager readerManager;

    private OutputCollector collector;
    private final String luceneIndexLocation;

    private static final Logger logger = Logger.getLogger(NewsItemToTermsBolt.class);

    /**
     *
     * @param luceneIndexLocation The full path to the lucene index folder.
     */
    public NewsItemToTermsBolt(String luceneIndexLocation) {
        this.luceneIndexLocation = luceneIndexLocation;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        try {
            this.collector = collector;
            Directory dir = FSDirectory.open(new File(luceneIndexLocation));
            searcherManager = new SearcherManager(dir, null);
            readerManager = new ReaderManager(dir);
        } catch (IOException ex) {
            logger.fatal(ex);
        }
    }

    @Override
    public void execute(Tuple input) {
        collector.ack(input);

        logger.debug(input);

        DirectoryReader reader = null;
        IndexSearcher searcher = null;

        try {
            readerManager.maybeRefreshBlocking();
            searcherManager.maybeRefreshBlocking();
            reader = readerManager.acquire();
            searcher = searcherManager.acquire();

            NewsItem item = (NewsItem) input.getValueByField(StreamIDs.INDEXEDITEM);
            logger.info("Extracting terms from document " + item.getId());

            Map<String, Double> terms = getTopterms(reader, searcher, item.getId());
            logger.info("Found " + terms.size() + " terms");

            for (String term : terms.keySet()) {
                logger.debug("emitted term: " + term);
                collector.emit(StreamIDs.TERMSTREAM, new Values(term));
            }

            

        } catch (IOException ex) {
            logger.error(ex);
        } finally {
            try {
                readerManager.release(reader);
                searcherManager.release(searcher);
            } catch (IOException | NullPointerException ex) {
                logger.error(ex);
            }
        }

    }

    private Map<String, Double> getTopterms(DirectoryReader reader, IndexSearcher searcher, long id) throws IOException {
        // fetch the terms occuring in this document
        Map<String, Double> termMap = new HashMap<>(250);

        updateTermMap(reader, searcher, termMap, id, "text", 1);
        // terms in the title adn description are more important than terms in the text.
        updateTermMap(reader, searcher, termMap, id, "description", 1.5);
        updateTermMap(reader, searcher, termMap, id, "title", 2);
        updateTermMap(reader, searcher, termMap, id, "term", 4);

        Map<String, Double> termsToStore = new HashMap<>();
        if (termMap.size() > 0) {
            // Only store the n most important terms.
            PriorityQueue<TermScorePair> pq = new PriorityQueue<>(termMap.size());
            double avg = 0;
            for (double d : termMap.values()) {
                avg += d;
            }
            avg /= termMap.size();

            double score;
            for (String term : termMap.keySet()) {
                score = termMap.get(term);
                if (score > avg) {
                    TermScorePair p = new TermScorePair(term, score);
                    pq.add(p);
                }

            }

            int n = (pq.size() < 10 ? pq.size() : 10);
            int i = 0;
            TermScorePair tsp = pq.poll();

            while (i < n && tsp != null) {
                termsToStore.put(tsp.getTerm(), tsp.getScore());
                tsp = pq.poll();
                i++;
            }
        }
        return termsToStore;
    }

    private void updateTermMap(DirectoryReader reader, IndexSearcher searcher, Map<String, Double> termMap, long id, String field, double weight) throws IOException {
        Query query = NumericRangeQuery.newLongRange("id", id, id, true, true);
        TopDocs topdocs = searcher.search(query, 1);

        if (topdocs.totalHits > 0) {
            int docNr = topdocs.scoreDocs[0].doc;
            Terms vector = reader.getTermVector(docNr, field);
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
                    //double minFreq = 0;
                    //double maxFreq = Double.MAX_VALUE;

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
            } else {
                logger.debug("no term available for doc=" + docNr + " and field=" + field);
            }
        } else {
            logger.warn("No documents found with id=" + id);
        }
    }
}
