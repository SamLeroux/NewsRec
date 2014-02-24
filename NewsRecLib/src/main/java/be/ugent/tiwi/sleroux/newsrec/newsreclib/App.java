package be.ugent.tiwi.sleroux.newsrec.newsreclib;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
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
import java.net.MalformedURLException;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

public class App {

    private static final Logger logger = Logger.getLogger(App.class);
    private static final ResourceBundle bundle = ResourceBundle.getBundle("newsRec");

    public static void main(String[] args) throws MalformedURLException, IOException, RatingsDaoException {

        NewsFetchTimer timer = getTimer();

        timer.start();
        try {
            Thread.sleep(480000);
        } catch (InterruptedException ex) {
            logger.fatal(ex.getMessage(), ex);
        }

        timer.stop();
        
//        testrecommendation();
//        testScoring();
        

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
    
    public static void testrecommendation(){
        try {
            DaoRecommender rec = new LuceneRecommender(bundle.getString("luceneIndexLocation"));
            rec.setRatingsDao(new JDBCRatingsDao());
            List<NewsItem> results = rec.recommend(1L, 0, 10);
            for(NewsItem item: results){
                System.out.println(item.getTitle());
            }
        } catch (IOException | RecommendationException | RatingsDaoException ex) {
            logger.error(ex);
        }
    }

    private static void testScoring() throws IOException, RatingsDaoException {
        IRatingsDao dao = new JDBCRatingsDao();
        IScorer scorer = new DatabaseLuceneScorer(bundle.getString("luceneIndexLocation"), dao);
        scorer.view(1L, 4789);
    }

}
