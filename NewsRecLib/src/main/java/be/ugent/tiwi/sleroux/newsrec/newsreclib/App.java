package be.ugent.tiwi.sleroux.newsrec.newsreclib;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.AbstractNewsfetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.NewsFetchTimer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.RssNewsFetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance.TikaEnhancer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsindex.LuceneNewsIndexer;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

public class App {

    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws MalformedURLException {

        INewsSourceDao dao = new MysqlNewsSourceDao();

        AbstractNewsfetcher fetcher = new RssNewsFetcher();
        fetcher.addEnhancer(new TikaEnhancer());

        NewsFetchTimer timer = new NewsFetchTimer(dao, fetcher, 10000);
        try {
            timer.addListener(new LuceneNewsIndexer("/home/sam/Bureaublad/index/"));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        timer.start();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
        }

        timer.stop();

    }
}
