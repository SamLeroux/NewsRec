/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RandomProfileTestingAgent extends ProfileTestingAgent {

    private static final Random rnd = new Random();

    public RandomProfileTestingAgent(RecommenderAccess access, long id) {
        super(access, id, new String[]{});
        discover();
    }

    private void discover() {
        NewsItemCluster[] clusters = access.getRecommendations();
        List<String> intr = new ArrayList<>();
        while (intr.isEmpty()) {
            for (NewsItemCluster c : clusters) {
                if (rnd.nextInt(15) == 0) {
                    for (NewsItem item : c.getItems()) {
                        if (rnd.nextInt(10) == 0) {
                            intr.addAll(item.getTerms().keySet());
                        }
                    }
                }
            }
        }
        intrests = intr.toArray(new String[intr.size()]);
        System.out.print(id);
        for (String intres : intrests) {
            System.out.print(";" + intres);
        }
        System.out.println();
    }

}
