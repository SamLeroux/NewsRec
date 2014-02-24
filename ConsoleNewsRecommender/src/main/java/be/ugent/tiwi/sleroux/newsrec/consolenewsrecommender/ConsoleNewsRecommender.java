/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.consolenewsrecommender;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.AbstractNewsfetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.INewsItemListener;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.NewsFetchTimer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.RssNewsFetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance.TikaEnhancer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsindex.LuceneNewsIndexer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.DaoRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.LuceneRecommender;
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
    private DaoRecommender rec;
    private long userid = 2L;

    public ConsoleNewsRecommender() {
        try {
            IRatingsDao dao = new JDBCRatingsDao();
            scorer = new DatabaseLuceneScorer(bundle.getString("luceneIndexLocation"), dao);
            rec = new LuceneRecommender(bundle.getString("luceneIndexLocation"));
            rec.setRatingsDao(dao);
        } catch (RatingsDaoException | IOException ex) {
            java.util.logging.Logger.getLogger(ConsoleNewsRecommender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        try {
            score(3738);
            score(3735);
            score(2443);
            score(2118);
            
            testrecommendation();
        } catch (RatingsDaoException | RecommendationException ex) {
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
            System.out.println(item.getTitle());
        }
    }

    private void score(int item) throws RatingsDaoException {
        scorer.view(userid, item);
    }

}
