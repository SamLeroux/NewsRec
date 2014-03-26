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
package be.ugent.tiwi.sleroux.newsrec.consolenewsrecommender;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.IClusterer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.LingPipeHierarchicalClustering;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCTrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.IRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.RecommendationException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.NewsFetchTopologyStarter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.RecommenderBuildException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.RecommenderBuilder;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ConsoleNewsRecommender {

    private static final Logger logger = Logger.getLogger(ConsoleNewsRecommender.class);
    private static final ResourceBundle bundle = ResourceBundle.getBundle("newsRec");
    
    private IScorer scorer;
    private IRecommender rec;
    private final long userid = 4L;
    private IViewsDao viewsDao;
    private ITrendsDao trendsDao;
    private IRatingsDao ratingsDao;
    private RecommenderBuilder builder;
    private final String luceneLoc = "/home/sam/index";
    private final String stopwordsFileLocation = "/home/sam/stopwords_EN.txt";

    public ConsoleNewsRecommender() {
        try {
            ratingsDao = new JDBCRatingsDao();
            trendsDao = new JDBCTrendsDao();
            viewsDao = new JDBCViewsDao();
            
            builder = new RecommenderBuilder(ratingsDao, trendsDao, viewsDao, luceneLoc);
     
            scorer = builder.getScorer(RecommenderBuilder.Scorers.WithTrendsFilter);
            rec = builder.getRecommender(RecommenderBuilder.Recommenders.TrendingAndPersonal);
        } catch (DaoException | RecommenderBuildException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void start() {
        try {     
            //startAddArticles();
            //startRectest();
            startFetchTest();
            //testClustering();
            builder.close();
        } catch (RecommenderBuildException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public void startAddArticles(){
        try {
            score(16100, "-4959261087675326762");
        } catch (DaoException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void startRectest(){
        try {
            testrecommendation();
        } catch (RecommendationException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testClustering() throws RecommendationException{
        List<NewsItem> results = rec.recommend(userid, 0, 250);
        long start = System.currentTimeMillis();
        IClusterer clusterer = new LingPipeHierarchicalClustering();
        List<NewsItemCluster> clusters = clusterer.cluster(results);
        System.out.println("Time: "+(System.currentTimeMillis()- start));
        System.out.println(clusters.size() + " clusters");
        for (NewsItemCluster cluster: clusters){
            System.out.println(cluster.getRepresentative().getTitle() + " ( "+cluster.getSize()+") members");
            for (NewsItem item: cluster.getItems()){
                System.out.println("\t"+item.getTitle());
            }
        }
        
    }
   

    public void testrecommendation() throws RecommendationException {
        List<NewsItem> results = rec.recommend(userid, 0, 20);
        for (NewsItem item : results) {
            System.out.print(item.getTitle());
            System.out.print(" : ");
            System.out.println(item.getUrl());
        }
    }

    private void score(int docNr, String itemid) throws DaoException {
        scorer.view(userid, itemid);
        viewsDao.see(userid, docNr, itemid);
    }

    private void startFetchTest() {
        try {
            INewsSourceDao newsSourceDao = new MysqlNewsSourceDao();
            
            
            NewsFetchTopologyStarter starter = new NewsFetchTopologyStarter(
                    newsSourceDao,
                    trendsDao,
                    "newsfetch",
                    luceneLoc,
                    stopwordsFileLocation);
            
            starter.start();
            Thread.sleep(1000*60*60*24);
            starter.stop();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
