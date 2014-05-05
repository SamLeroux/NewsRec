/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.newsreccollaborativefiltering;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class MahoutDataFileWriter {
    private final IRatingsDao ratingsDao;
    private final Writer writer;

    public MahoutDataFileWriter(IRatingsDao ratingsDao, String outputFileName) throws IOException {
        this.ratingsDao = ratingsDao;
        writer = new FileWriter(outputFileName);
    }
    
    public String[] writeOutputFile() throws RatingsDaoException, IOException{
        int id = 0;
        HashMap<String, Integer> terms = new HashMap<>();
        
        Map<Long, Map<String, Double>> ratings = ratingsDao.getAllRatings();
        for (long user: ratings.keySet()){
            Map<String, Double> termsForUser = ratings.get(user);
            for (String term: termsForUser.keySet()){
                if (!terms.containsKey(term)){
                    terms.put(term, id);
                    id++;
                }
                writer.write(Long.toString(user));
                writer.write(";");
                writer.write(Integer.toString(terms.get(term)));
                writer.write(";");
                writer.write(Double.toString(termsForUser.get(term)));
                writer.write("\n");
            }
        }
        
        writer.close();
        
        String[] output = new String[id];
        for (String s: terms.keySet()){
            output[terms.get(s)] = s;
        }
        
        return output;
    }
}
