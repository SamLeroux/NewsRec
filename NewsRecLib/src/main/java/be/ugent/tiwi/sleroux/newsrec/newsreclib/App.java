package be.ugent.tiwi.sleroux.newsrec.newsreclib;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.dummyImpl.DummyNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsSource;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.NewsFetchException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.RssNewsFetcher;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.TikaEnhancer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class App 
{
    public static void main( String[] args )
    {
        INewsSourceDao dao = new DummyNewsSourceDao();
        RssNewsFetcher fetcher = new RssNewsFetcher();
        fetcher.addEnhancer(new TikaEnhancer());
        for (NewsSource source: dao.getSourcesToCheck()){
            try {
                fetcher.fetch(source);
            } catch (NewsFetchException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
