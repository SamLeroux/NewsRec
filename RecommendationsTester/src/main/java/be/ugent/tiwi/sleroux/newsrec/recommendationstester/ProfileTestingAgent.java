/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import java.util.HashSet;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ProfileTestingAgent implements ITestingAgent{

    protected final RecommenderAccess access;
    protected final long id;
    protected String[] intrests;
    protected final HashSet<String> seen;

    public ProfileTestingAgent(RecommenderAccess access, long id, String[] intrests) {
        this.access = access;
        this.id = id;
        this.intrests = intrests;
        seen = new HashSet<>();
    }

    @Override
    public TestResult test() {
        access.logIn(id);
        int relevant = 0;
        int relevantNotYetSeen = 0;
        long start = System.currentTimeMillis();
        NewsItemCluster[] results = access.getRecommendations();
        int time = (int) (System.currentTimeMillis() - start);
        for (NewsItemCluster cluster : results) {
            int c = 0;
            boolean stop = false;
            while (!stop && c < intrests.length) {
                stop = cluster.getRepresentative().getTitle().toLowerCase().contains(intrests[c]);
                stop = stop || cluster.getRepresentative().getDescription().toLowerCase().contains(intrests[c]);
                c++;
            }
            if (stop) {
                relevant++;
                if (!seen.contains(cluster.getRepresentative().getId())) {
                    access.view(cluster.getRepresentative().getId(), cluster.getRepresentative().getDocNr());
                    seen.add(cluster.getRepresentative().getId());
                    relevantNotYetSeen++;
                }
            }
        }

        TestResult r = new TestResult(results.length, relevant, relevantNotYetSeen, time);
        return r;

    }

}
