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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.apache.lucene.search.SearcherManager;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DatabaseLuceneScorerWithTrendsFilter extends DatabaseLuceneScorer {

    private final ITrendsDao trendsDao;
    private HashSet<String> trends;
    private int cnt = 0;
    private static final Logger logger = Logger.getLogger(DatabaseLuceneScorerWithTrendsFilter.class);

    public DatabaseLuceneScorerWithTrendsFilter(SearcherManager manager, IRatingsDao dao, ITrendsDao trendsDao) {
        super(manager, dao);
        this.trendsDao = trendsDao;
        trends = new HashSet<>();
    }

    @Override
    public void view(long user, String item) {
        maybeUpdateTrends();
        if (!trends.contains(item)) {
            super.view(user, item);
        }
    }

    @Override
    public void score(long user, String item, double rating) {
        maybeUpdateTrends();
        if (!trends.contains(item)) {
            super.score(user, item, rating);
        }
    }

    private void maybeUpdateTrends() {
        if (cnt == 0) {
            try {
                trends.clear();
                trends.addAll(Arrays.asList(trendsDao.getTrends()));
            } catch (TrendsDaoException ex) {
                logger.error(ex);
            }
        }
        cnt++;
        cnt = cnt % 10;
    }
}
