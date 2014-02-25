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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class CombinedRecommender implements IRecommender{
    
    private final List<IRecommender> recommenders;

    public CombinedRecommender() {
        recommenders = new ArrayList<>();
    }
    
    public void addRecommender(IRecommender r){
        recommenders.add(r);
    }
    
    

    @Override
    public List<NewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        if (!recommenders.isEmpty()){
            List<NewsItem> results = recommenders.get(0).recommend(userid, start, count);
            for (int i = 1; i < recommenders.size();i++){
                results.addAll(recommenders.get(i).recommend(userid, start, count));
            }
            return results;
        }
        return new ArrayList();
    }
    
}
