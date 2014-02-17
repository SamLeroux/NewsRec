package be.ugent.tiwi.sleroux.newsrec.newsreclib;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlDao.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.AbstractNewsfetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.NewsFetchException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.RssNewsFetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.TikaEnhancer;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) throws MalformedURLException {
        INewsSourceDao dao = new MysqlNewsSourceDao();

//        NewsSource n = new NewsSource();
//        n.setName("CNN - Top Stories");
//        n.setRssUrl(new URL("http://rss.cnn.com/rss/edition.rss"));
//        n.setFetchinterval(5);
//        dao.AddNewsSource(n);
        
        AbstractNewsfetcher fetcher = new RssNewsFetcher();
        fetcher.addEnhancer(new TikaEnhancer());
               
        for (NewsSource source : dao.getSourcesToCheck()) {
            try {
                fetcher.fetch(source);
            } catch (NewsFetchException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(source);
        }
        dao.close();
        
        
    }
}
