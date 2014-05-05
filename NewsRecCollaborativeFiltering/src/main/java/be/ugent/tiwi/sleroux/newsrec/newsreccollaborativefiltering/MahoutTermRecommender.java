/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.newsreccollaborativefiltering;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class MahoutTermRecommender {

    private final String mahoutInputFile;

    public MahoutTermRecommender(String mahoutInputFile) {
        this.mahoutInputFile = mahoutInputFile;
    }

    public Map<Long, List<RecommendedItem>> makeRecommendations(int n) throws IOException, TasteException {

        DataModel model = new FileDataModel(new File(mahoutInputFile), ";");
        UserSimilarity similarity = new TanimotoCoefficientSimilarity(model);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);
        Recommender recommender = new GenericUserBasedRecommender(
                model, neighborhood, similarity);

        LongPrimitiveIterator it = model.getUserIDs();
        Map<Long, List<RecommendedItem>> output = new HashMap<>(model.getNumUsers());

        while (it.hasNext()) {
            long user = it.nextLong();
            List<RecommendedItem> items = recommender.recommend(user, n);
            output.put(user, items);
        }

        return output;
    }
}
