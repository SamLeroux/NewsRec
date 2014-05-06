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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.RecommendedNewsItem;
import java.io.IOException;
import java.util.List;
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

    private final List<RecommendedNewsItem> items;

    public UniqueResultsFilter(List<RecommendedNewsItem> items) {
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

        for (RecommendedNewsItem item : items) {
            int relative = item.getDocNr() - docBase; // relative id in this context
            if (relative >= 0 && relative < maxId) {
                bits.fastClear(item.getDocNr() - docBase);
            }
        }
        return bits;
    }

}
