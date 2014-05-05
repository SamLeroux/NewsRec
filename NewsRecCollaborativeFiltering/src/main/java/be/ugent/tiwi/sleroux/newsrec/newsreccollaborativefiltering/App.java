/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.newsreccollaborativefiltering;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class App {
    private static final String mahoutInputFile = "/home/sam/ratings.csv";
    
    public static void main(String[] args) throws DaoException, IOException, TasteException {
        IRatingsDao ratingsDao = new JDBCRatingsDao();
        
        MahoutDataFileWriter fileWriter = new MahoutDataFileWriter(ratingsDao, mahoutInputFile);
        String[] ids = fileWriter.writeOutputFile();
        
        MahoutTermRecommender recommender = new MahoutTermRecommender(mahoutInputFile);
        Map<Long, List<RecommendedItem>> recommendations = recommender.makeRecommendations(10);
    
        for (Long user: recommendations.keySet()){
            List<RecommendedItem> items = recommendations.get(user);
            System.out.println(user);
            for (RecommendedItem item: items){
                System.out.println(ids[(int)item.getItemID()]+"\t"+item.getValue());
            }
            System.out.println("");
        }
        
        RecommendationsToDatabase r2db = new RecommendationsToDatabase(ratingsDao);
        r2db.store(ids, recommendations);
    }
}
