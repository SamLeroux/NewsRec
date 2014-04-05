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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.NewsItemLuceneDocConverter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.SearcherManager;

/**
 * Recommend newsitems by creating a query and issuing it to Lucene.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public abstract class LuceneRecommender implements IRecommender {

    protected SearcherManager manager;

    private static final Logger logger = Logger.getLogger(LuceneRecommender.class);

    static {
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
    }

    public LuceneRecommender(SearcherManager manager) {
        this.manager = manager;
    }

    protected NewsItem toNewsitem(Document d, int docId) throws IOException {
        logger.debug("Converting document to newsitem");
        NewsItem item = NewsItemLuceneDocConverter.DocumentToNewsItem(d);
        item.setDocNr(docId);
        return item;
    }

}
