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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.filters;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class UniqueResultsFilter extends Filter {

    private final List<NewsItem> items;
    private static final Logger logger = Logger.getLogger(UniqueResultsFilter.class);

    public UniqueResultsFilter(List<NewsItem> items) {
        this.items = items;
    }

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        // The context only represents a single index not the complete underlying index.
        // We need to calculate the relative id in this part of the index.
        AtomicReader reader = context.reader();
        int maxId = reader.maxDoc();
        int docBase = context.docBase;

        OpenBitSet bits = new OpenBitSet(maxId);
        // Mark all documents as active
        bits.set(0, maxId);

        for (NewsItem item : items) {
            int relative = item.getDocNr() - docBase; // relative id in this context
            if (relative >= 0 && relative < maxId) {
                logger.debug("cleared " + item.getDocNr() + ", should not show up in results");
                // Remove this document from the results.
                bits.fastClear(item.getDocNr() - docBase);
            }
        }
        return bits;
    }

}
