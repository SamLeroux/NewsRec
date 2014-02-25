/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.consolenewsrecommender;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.AbstractNewsfetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.INewsItemListener;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.NewsFetchTimer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.RssNewsFetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance.TikaEnhancer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsindex.LuceneNewsIndexer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.ColdStartLuceneRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.IRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.RecommendationException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.DatabaseLuceneScorer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class ConsoleNewsRecommender {

    private static final Logger logger = Logger.getLogger(be.ugent.tiwi.sleroux.newsrec.newsreclib.App.class);
    private static final ResourceBundle bundle = ResourceBundle.getBundle("newsRec");
    private IScorer scorer;
    private IRecommender rec;
    private final long userid = 4L;
    private IViewsDao viewsDao;

    public ConsoleNewsRecommender() {
        try {
            IRatingsDao dao = new JDBCRatingsDao();
            String luceneLoc = bundle.getString("luceneIndexLocation");
            scorer = new DatabaseLuceneScorer(luceneLoc, dao);
            viewsDao = new JDBCViewsDao();
            //rec = new LuceneTermRecommender(bundle.getString("luceneIndexLocation"),dao, viewsDao);
            //rec = new TopNRecommender(bundle.getString("luceneIndexLocation"), viewsDao);
            //rec.setRatingsDao(dao);
//            rec = new CombinedRecommender();
//            rec.addRecommender(new LuceneTermRecommender(bundle.getString("luceneIndexLocation"),dao, viewsDao));
//            rec.addRecommender(new TopNRecommender(bundle.getString("luceneIndexLocation"), viewsDao));
            rec = new ColdStartLuceneRecommender(luceneLoc, dao, viewsDao);
        } catch (RatingsDaoException | IOException | ViewsDaoException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void start() {
        startAddArticles();
        startRectest();
        //startFetchTest();
    }
    
    public void startAddArticles(){
        try {
            score(8290, -6987741477396414561L);
            score(8291, -7520079592792343477L);
            score(8293, -5002904209232001034L);
            score(9100, -5193966549208919344L);
            score(9098, -9159839562141742758L);
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

    public static NewsFetchTimer getTimer() {
        INewsSourceDao dao = new MysqlNewsSourceDao();

        AbstractNewsfetcher fetcher = new RssNewsFetcher();
        fetcher.addEnhancer(new TikaEnhancer());

        NewsFetchTimer timer = new NewsFetchTimer(dao, fetcher, 10000);
        try {
            String luceneIndex = bundle.getString("luceneIndexLocation");
            String stopwordFile = bundle.getString("stopwordsFile");
            INewsItemListener luceneListener = new LuceneNewsIndexer(luceneIndex, stopwordFile);
            timer.addListener(luceneListener);
        } catch (IOException ex) {
            logger.fatal(ex.getMessage(), ex);
        }
        return timer;
    }

    public void testrecommendation() throws RecommendationException {
        List<NewsItem> results = rec.recommend(userid, 0, 20);
        for (NewsItem item : results) {
            System.out.print(item.getTitle());
            System.out.print(" : ");
            System.out.println(item.getSource());
        }
    }

    private void score(int docNr, long itemid) throws DaoException {
        scorer.view(userid, docNr);
        viewsDao.see(userid, docNr, itemid);
    }

    private void startFetchTest() {
        try {
            NewsFetchTimer t = getTimer();
            t.start();
            Thread.sleep(60000);
            t.stop();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
