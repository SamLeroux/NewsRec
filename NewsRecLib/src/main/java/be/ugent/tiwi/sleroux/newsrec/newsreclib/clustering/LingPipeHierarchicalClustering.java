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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.util.Distance;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LingPipeHierarchicalClustering implements IClusterer {

    private static final Logger logger = Logger.getLogger(LingPipeHierarchicalClustering.class);

    @Override
    public List<NewsItemCluster> cluster(List<NewsItem> items) {

        HierarchicalClusterer<NewsItem> clusterer = new SingleLinkClusterer<>(new NewsItemDistance(items));
        Set<NewsItem> itemSet = new HashSet<>(items);
        Dendrogram<NewsItem> dend = clusterer.hierarchicalCluster(itemSet);
        Set<Set<NewsItem>> clusters = dend.partitionDistance(0.85);

        List<NewsItemCluster> clusterList = new ArrayList<>(clusters.size());
        for (Set<NewsItem> sn : clusters) {
            NewsItemCluster nc = new NewsItemCluster();
            for (NewsItem ni : sn) {
                nc.addItem(ni);
            }
            clusterList.add(nc);
        }
        return clusterList;
    }

    private class NewsItemDistance implements Distance {

        List<HashSet<String>> cache;

        public NewsItemDistance(List<NewsItem> items) {
            cache = new ArrayList<>(items.size());
            for (NewsItem item : items) {
                cache.add(new HashSet<>(item.getTerms().keySet()));
            }

        }

        @Override
        public double distance(Object e, Object e1) {
            NewsItem n1 = (NewsItem) e;
            NewsItem n2 = (NewsItem) e1;

            Set<String> obs1 = cache.get(n1.getDocNr());
            Set<String> obs2 = cache.get(n2.getDocNr());

            if (obs1.isEmpty() || obs2.isEmpty()) {
                logger.warn("distance between " + n1.getId() + " and " + n2.getId() + " not defined, termset is empty");
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

}
