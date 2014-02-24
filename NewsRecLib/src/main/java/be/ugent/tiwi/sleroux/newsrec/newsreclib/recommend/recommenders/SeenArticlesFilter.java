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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.ViewsDaoException;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class SeenArticlesFilter extends Filter {

    private final IViewsDao viewsdao;
    private final long userId;
    private static final Logger logger = Logger.getLogger(SeenArticlesFilter.class);

    public SeenArticlesFilter(IViewsDao viewsdao, long userId) {
        this.viewsdao = viewsdao;
        this.userId = userId;
    }

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        OpenBitSet bits = new OpenBitSet(context.reader().maxDoc());
        bits.set(0, bits.size());
        try {
            List<Integer> viewed = viewsdao.getSeenArticles(userId);
            for (int i : viewed) {
                bits.clear(i);
            }
        } catch (ViewsDaoException ex) {
            logger.error(ex);
        }
        return bits;
    }

}
