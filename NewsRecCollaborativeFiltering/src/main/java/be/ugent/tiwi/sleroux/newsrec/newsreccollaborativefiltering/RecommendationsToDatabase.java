/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.newsreccollaborativefiltering;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecommendationsToDatabase {

    private final IRatingsDao ratingsDao;

    public RecommendationsToDatabase(IRatingsDao ratingsDao) {
        this.ratingsDao = ratingsDao;
    }

    public void store(String[] terms, Map<Long, List<RecommendedItem>> recommendations) throws RatingsDaoException {
        for (Long user : recommendations.keySet()) {
            List<RecommendedItem> items = recommendations.get(user);
            if (items.size() > 0) {
                Map<String, Double> scoreMap = new HashMap<>();
                for (RecommendedItem item : items) {
                    String term = terms[(int) item.getItemID()];
                    double score = item.getValue();
                    scoreMap.put(term, score);
                }
                ratingsDao.giveRating(user, scoreMap);
            }
        }
    }
}
