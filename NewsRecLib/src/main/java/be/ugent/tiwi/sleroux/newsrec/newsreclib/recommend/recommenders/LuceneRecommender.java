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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Recommend newsitems by creating a query and issuing it to Lucene.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LuceneRecommender extends DaoRecommender {

    private String luceneIndexLocation;
    private Directory dir;
    private IndexReader reader;
    private IndexSearcher searcher;
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

    @Override
    public List<NewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        try {
            Map<String, Double> terms = getRatingsDao().getRatings(userid);
            Query query = buildQuery(terms);
            int hitsPerPage = 100000;

            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage,true);
            try {
                Filter filter = new SeenArticlesFilter(new JDBCViewsDao(), userid);
                searcher.search(query, filter, collector);
            } catch (ViewsDaoException ex) {
                logger.error(ex);
                searcher.search(query, collector);
            }
            
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            int stop = (start + count < hits.length ? start + count : hits.length);
            List<NewsItem> results = new ArrayList<>(stop-start);
            
            for (int i = start; i < stop; i++) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                results.add(toNewsitem(d));
            }

            return results;

        } catch (RatingsDaoException | IOException ex) {
            logger.error(ex);
            throw new RecommendationException(ex);
        }
    }

    private Query buildQuery(Map<String, Double> terms) {
        BooleanQuery q = new BooleanQuery();

        BooleanQuery.setMaxClauseCount(terms.size() * 2);

        for (String term : terms.keySet()) {
            Query query = new TermQuery(new Term("text", term));
            query.setBoost(terms.get(term).floatValue());
            q.add(query, BooleanClause.Occur.SHOULD);
        }
        
        //return q;
        return new RecencyBoostQuery(q);
    }

    private void openIndex() throws IOException {
        dir = FSDirectory.open(new File(luceneIndexLocation));
        reader = DirectoryReader.open(dir, 1);
        searcher = new IndexSearcher(reader);
    }

    private NewsItem toNewsitem(Document d) {
        logger.debug("Converting document to newsitem");
        NewsItem item = new NewsItem();

        IndexableField field;

        field = d.getField("description");
        if (field != null) {
            item.setDescription(field.stringValue());
        } else {
            item.setDescription("No description available");
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

        item.setSource(null);

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
