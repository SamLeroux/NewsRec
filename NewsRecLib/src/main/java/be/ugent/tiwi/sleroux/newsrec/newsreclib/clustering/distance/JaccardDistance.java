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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JaccardDistance implements IDistance {

    private final List<HashSet<String>> cache;

    public JaccardDistance(List<NewsItem> items) {
        cache = new ArrayList<>(items.size());
        for (NewsItem item : items) {
            cache.add(new HashSet<>(item.getTerms().keySet()));
        }

    }

    @Override
    public double distance(NewsItem n1, NewsItem n2) {

        Set<String> obs1 = cache.get(n1.getDocNr());
        Set<String> obs2 = cache.get(n2.getDocNr());

        if (obs1.isEmpty() || obs2.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        } else {
            int c = 0;
            for (String s : obs1) {
                if (obs2.contains(s)) {
                    c++;
                }
            }
            double d = (double) (obs1.size() + obs2.size() - c - c) / (double) (obs1.size() + obs2.size());
            return d;
        }

    }

}
