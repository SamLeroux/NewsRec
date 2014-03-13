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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.search.SearcherManager;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ColdStartLuceneRecommender implements IRecommender{
    private final LuceneTermRecommender r1;
    private final TopNRecommender r2;

    public ColdStartLuceneRecommender(SearcherManager manager, IRatingsDao rdao, IViewsDao vdao) throws IOException {
        r1 = new LuceneTermRecommender(rdao, vdao, manager);
        r2 = new TopNRecommender(vdao, manager);
    }

    
    @Override
    public List<NewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        List<NewsItem> results = r1.recommend(userid, start, count);
        if (results.size() < count){
            List<NewsItem> results2 = r2.recommend(userid, start, count);
            int i = 0;
            while (results.size() < count && i < results2.size()){
                results.add(results2.get(i));
                i++;
            }
        }
        return results;
    }
}
