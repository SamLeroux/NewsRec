/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TestingAgent {
    private final RecommenderAccess access;
    private final long id;
    private final String[] intrests;
    private static final Random rnd = new Random();

    public TestingAgent(RecommenderAccess access, long id, String[] intrests) {
        this.access = access;
        this.id = id;
        this.intrests = intrests;
    }

   
    public void start(){
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TestingTask(), 0, 1000*60*1/2);
        access.logIn(id);
    }
    
    private class TestingTask extends TimerTask{

        @Override
        public void run() {
            int relevant = 0;
            NewsItemCluster[] results = access.getRecommendations();
            for (NewsItemCluster cluster: results){
                int c = 0;
                boolean stop = false;
                while (!stop && c < intrests.length){
                    stop = cluster.getRepresentative().getTitle().toLowerCase().contains(intrests[c]);
                    stop = stop || cluster.getRepresentative().getDescription().toLowerCase().contains(intrests[c]);
                    c++;
                }
                if (stop){
                    relevant++;
                    access.view(cluster.getRepresentative().getId(), cluster.getRepresentative().getDocNr());
                }
            }
            double score = ((double)relevant)/((double)results.length);
            System.out.println(new Date()+";"+score);
        }
    }   
    
}
