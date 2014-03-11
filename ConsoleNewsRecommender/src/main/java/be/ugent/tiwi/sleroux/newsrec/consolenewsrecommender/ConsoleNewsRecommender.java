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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.ColdStartLuceneRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.IRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.RecommendationException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.DatabaseLuceneScorer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.NewsFetchTopologyStarter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.TrendingTopicRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.topTerms.LuceneDocTopTermsExtract;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    private LuceneDocTopTermsExtract termExtract;

    public ConsoleNewsRecommender() {
        try {
            IRatingsDao dao = new JDBCRatingsDao();
            String luceneLoc = "/home/sam/Bureaublad/index2";
            scorer = new DatabaseLuceneScorer(luceneLoc, dao);
            viewsDao = new JDBCViewsDao();
            trendsDao = new JDBCTrendsDao();
            //rec = new LuceneTermRecommender(bundle.getString("luceneIndexLocation"),dao, viewsDao);
            //rec = new TopNRecommender(bundle.getString("luceneIndexLocation"), viewsDao);
            //rec.setRatingsDao(dao);
//            rec = new CombinedRecommender();
//            rec.addRecommender(new LuceneTermRecommender(bundle.getString("luceneIndexLocation"),dao, viewsDao));
//            rec.addRecommender(new TopNRecommender(bundle.getString("luceneIndexLocation"), viewsDao));
            //rec = new ColdStartLuceneRecommender(luceneLoc, dao, viewsDao);
            rec = new TrendingTopicRecommender(trendsDao, viewsDao, luceneLoc);
            termExtract = new LuceneDocTopTermsExtract(luceneLoc);
        } catch (IOException | DaoException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void start() {
     
            //startAddArticles();
            //startRectest();
            startFetchTest();
            //testClustering();
       
    }
    
    public void startAddArticles(){
        try {
            score(16100, -4959261087675326762L);
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
        for(NewsItem item: results){
            Map<String, Double> topTerms = termExtract.getTopTerms(item.getDocNr());
            for (String term: topTerms.keySet()){
                item.addTerm(term, topTerms.get(term).floatValue());
            }
        }
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

    private void score(int docNr, long itemid) throws DaoException {
        scorer.view(userid, docNr);
        viewsDao.see(userid, docNr, itemid);
    }

    private void startFetchTest() {
        try {
            INewsSourceDao newsSourceDao = new MysqlNewsSourceDao();
            
            String luceneIndexLocation = bundle.getString("luceneIndexLocation");
            String stopwordsFileLocation = bundle.getString("stopwordsFile");
            
            NewsFetchTopologyStarter starter = new NewsFetchTopologyStarter(
                    newsSourceDao,
                    trendsDao,
                    "newsfetch",
                    luceneIndexLocation,
                    stopwordsFileLocation);
            
            starter.start();
            Thread.sleep(1000*60*60*24);
            starter.stop();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
