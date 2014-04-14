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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance.DistanceAdapter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance.JaccardDistance;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LingPipeHierarchicalClustering implements IClusterer {

    @Override
    public List<NewsItemCluster> cluster(List<NewsItem> items) {

        HierarchicalClusterer<NewsItem> clusterer = new SingleLinkClusterer<>(new DistanceAdapter(new JaccardDistance(items)));
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
}
