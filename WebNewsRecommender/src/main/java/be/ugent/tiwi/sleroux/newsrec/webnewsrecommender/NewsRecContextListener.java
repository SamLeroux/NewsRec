/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.webnewsrecommender;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.ColdStartLuceneRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.IRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.DatabaseLuceneScorer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsRecContextListener implements ServletContextListener{

    private IViewsDao viewsDao;
    private IRatingsDao ratingsDao;
    private IRecommender recommender;
    private IScorer scorer;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            viewsDao = new JDBCViewsDao();
            sce.getServletContext().setAttribute("viewsDao", viewsDao);
            
            ratingsDao = new JDBCRatingsDao();
            
            String luceneLocation = "/home/sam/Bureaublad/index";
            recommender = new ColdStartLuceneRecommender(luceneLocation, ratingsDao, viewsDao); 
            sce.getServletContext().setAttribute("recommender", recommender);
            
            scorer = new DatabaseLuceneScorer(luceneLocation, ratingsDao);
            sce.getServletContext().setAttribute("scorer", scorer);
            
        } catch (RatingsDaoException ex) {
            Logger.getLogger(NewsRecContextListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ViewsDaoException ex) {
            Logger.getLogger(NewsRecContextListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewsRecContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
    
}
