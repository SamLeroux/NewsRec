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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsindex;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.config.Config;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.INewsItemListener;
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
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Listens for new articles and adds them to the Lucene index.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LuceneNewsIndexer implements INewsItemListener {

    private final IndexWriter writer;
    private static final Logger logger = Logger.getLogger(LuceneNewsIndexer.class);
    private final FieldType ftype;

    public LuceneNewsIndexer(String indexLocation, String stopwordsLocation) throws IOException {
        Directory dir = FSDirectory.open(new File(indexLocation));
        EnAnalyzer analyzer = new EnAnalyzer();
        analyzer.setStopwords(getStopwords(stopwordsLocation));
        IndexWriterConfig config = new IndexWriterConfig(Config.LUCENE_VERSION, analyzer);
        writer = new IndexWriter(dir, config);
        ftype = new FieldType();
        ftype.setIndexed(true);
        ftype.setStoreTermVectors(true);
        ftype.setStored(true);
        logger.info("Created lucene index");
    }

    @Override
    public void newItem(NewsItem[] items) {
        logger.debug(items.length + " new items to add to lucene index.");
        try {
            for (NewsItem item : items) {
                Document doc = new Document();
                if (item.getTitle() != null) {
                    doc.add(new Field("title", item.getTitle(), ftype));
                }
                if (item.getFulltext() != null) {
                    doc.add(new Field("text", item.getFulltext(), ftype));
                }
                if (item.getDescription() != null) {
                    doc.add(new Field("description", item.getDescription(), ftype));
                }

                doc.add(new LongField("id", item.getId(), Store.YES));

                if (item.getUrl() != null) {
                    doc.add(new StringField("url", item.getUrl().toString(), Store.YES));
                }
                if (item.getImageUrl() != null) {
                    doc.add(new StringField("imageUrl", item.getImageUrl().toString(), Store.YES));
                }
                if (item.getLocale() != null) {
                    doc.add(new StringField("locale", item.getLocale().getISO3Language(), Store.YES));
                }
                if (item.getSource() != null) {
                    doc.add(new StringField("source", item.getSource(), Store.YES));
                }
                if (item.getTimestamp() != null) {
                    doc.add(new LongField("timestamp", item.getTimestamp().getTime(), Store.YES));
                }

                for (String author : item.getAuthors()) {
                    doc.add(new StringField("author", author, Store.YES));
                }

                Map<String, Float> terms = item.getTerms();
                for (String term : terms.keySet()) {
                    TextField tf = new TextField("term", term, Store.YES);
                    tf.setBoost(terms.get(term));
                }

                writer.addDocument(doc);

            }
            writer.commit();
            logger.debug("commited new items");

        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private CharArraySet getStopwords(String stopwordsLocation) throws FileNotFoundException, IOException{
        logger.debug("reading stopwords file: "+stopwordsLocation);
        CharArraySet stopw;
        try (BufferedReader reader = new BufferedReader(new FileReader(stopwordsLocation))) {
            stopw = new CharArraySet(Config.LUCENE_VERSION, 1000, true);
            String line = reader.readLine();
            while (line != null){
                stopw.add(line);
                line = reader.readLine();
            }
        }
        
        return stopw;
    }
}
