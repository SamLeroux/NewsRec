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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance.IDistance;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance.JaccardDistance;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.RecommendedNewsItem;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ThresholdClusterer implements IClusterer {

    private int[] itemToCLuster;
    private List<List<Integer>> clusterToItems;

    @Override
    public List<NewsItemCluster> cluster(List<RecommendedNewsItem> items) {
        itemToCLuster = new int[items.size()];
        clusterToItems = new ArrayList<>(items.size());

        for (int i = 0; i < items.size(); i++) {
            items.get(i).setRecommendationId(i);
            itemToCLuster[i] = i;
            List<Integer> l = new LinkedList<>();
            l.add(i);
            clusterToItems.add(l);
        }

        IDistance d = new JaccardDistance(items);

        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if (itemToCLuster[i] != itemToCLuster[j]) {
                    double distance = d.distance(items.get(i), items.get(j));
                    if (distance < 0.85) {
                        int c1 = itemToCLuster[i];
                        int c2 = itemToCLuster[j];

                        int s1 = clusterToItems.get(c1).size();
                        int s2 = clusterToItems.get(c2).size();

                        if (s1 <= s2) {
                            clusterToItems.get(c2).addAll(clusterToItems.get(c1));
                            for (Integer a : clusterToItems.get(c1)) {
                                itemToCLuster[a] = c2;
                            }
                            clusterToItems.get(c1).clear();

                        } else {
                            clusterToItems.get(c1).addAll(clusterToItems.get(c2));
                            for (Integer a : clusterToItems.get(c2)) {
                                itemToCLuster[a] = c2;
                            }
                            clusterToItems.get(c2).clear();
                        }
                    }
                }
            }
        }
        List<NewsItemCluster> clusters = new ArrayList<>();
        for (List<Integer> l : clusterToItems) {
            if (!l.isEmpty()) {
                NewsItemCluster c = new NewsItemCluster();
                for (Integer i : l) {
                    c.addItem(items.get(i));
                }
                clusters.add(c);
            }
        }
        return clusters;

    }
}
