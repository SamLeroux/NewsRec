package be.ugent.tiwi.sleroux.newsrec.newsreclib;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.AbstractNewsfetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.INewsItemListener;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.NewsFetchTimer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.RssNewsFetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance.DummyEnhancer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance.TikaEnhancer;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) throws MalformedURLException {

        INewsSourceDao dao = new MysqlNewsSourceDao();

//        NewsSource n = new NewsSource();
//        n.setName("CNN - Top Stories");
//        n.setRssUrl(new URL("http://rss.cnn.com/rss/edition_world.rss"));
//        n.setFetchinterval(120000);
//        dao.startSession();
//        dao.AddNewsSource(n);
//        dao.stopSession();
        AbstractNewsfetcher fetcher = new RssNewsFetcher();
        //fetcher.addEnhancer(new TikaEnhancer());
        fetcher.addEnhancer(new DummyEnhancer());

        NewsFetchTimer timer = new NewsFetchTimer(dao, fetcher, 10000);
        timer.addListener(new INewsItemListener() {

            @Override
            public void newItem(NewsItem item) {
                System.out.println("a");
            }

            @Override
            public void newItem(NewsItem[] items) {
                System.out.println(items.length);
            }
        });
        timer.start();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        timer.stop();

    }
}
