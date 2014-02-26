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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Recommend newsitems by creating a query and issuing it to Lucene.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public abstract class LuceneRecommender implements IRecommender {

    private String luceneIndexLocation;
    private Directory dir;
    protected SearcherManager manager;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LuceneRecommender.class);

    public LuceneRecommender(String luceneIndexLocation) throws IOException {
        this.luceneIndexLocation = luceneIndexLocation;
        openIndex();
    }

    public String getLuceneIndexLocation() {
        return luceneIndexLocation;
    }

    public void setLuceneIndexLocation(String luceneIndexLocation) {
        this.luceneIndexLocation = luceneIndexLocation;
    }

    private void openIndex() throws IOException {
        dir = FSDirectory.open(new File(luceneIndexLocation));
        manager = new SearcherManager(dir,null);     
        
//        reader = DirectoryReader.open(dir, 1);
//        searcher = new IndexSearcher(reader);
//        searcher.setSimilarity(new DefaultSimilarity());
    }

    protected NewsItem toNewsitem(Document d, int docId) {
        logger.debug("Converting document to newsitem");
        NewsItem item = new NewsItem();

        IndexableField field;

        field = d.getField("description");
        if (field != null) {
            item.setDescription(field.stringValue());
        } else {
            item.setDescription("No description available");
        }
        
        field = d.getField("source");
        if (field != null) {
            item.setSource(field.stringValue());
        } else {
            item.setSource("No source available");
        }

        field = d.getField("text");
        if (field != null) {
            item.setFulltext(field.stringValue());
        } else {
            item.setFulltext("No text available");
        }

        field = d.getField("id");
        if (field != null) {
            item.setId(field.numericValue().longValue());
        } else {
            item.setId(0);
        }

        item.setDocNr(docId);
        
        field = d.getField("imageUrl");
        if (field != null) {
            try {
                item.setImageUrl(new URL(field.stringValue()));
            } catch (MalformedURLException ex) {
                logger.debug(field.stringValue());
                logger.error(ex.getMessage(), ex);
                item.setImageUrl(null);
            }
        }

        field = d.getField("locale");
        if (field != null) {
            item.setLocale(Locale.forLanguageTag(field.stringValue()));
        } else {
            item.setLocale(Locale.getDefault());
        }

    

        field = d.getField("timestamp");
        if (field != null) {
            item.setTimestamp(new Date(field.numericValue().longValue()));
        } else {
            item.setTimestamp(new Date());
        }

        field = d.getField("title");
        if (field != null) {
            item.setTitle(field.stringValue());
        } else {
            item.setTitle("");
        }

        field = d.getField("url");
        if (field != null) {
            try {
                item.setUrl(new URL(field.stringValue()));
            } catch (MalformedURLException ex) {
                logger.debug(field.stringValue());
                logger.error(ex.getMessage(), ex);
                item.setUrl(null);
            }
        } else {
            item.setTitle("");
        }

        return item;
    }

}
