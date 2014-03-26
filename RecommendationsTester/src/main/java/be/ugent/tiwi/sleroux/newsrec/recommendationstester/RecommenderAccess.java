/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tika.io.IOUtils;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecommenderAccess {

    private final String baseUrl;
    private String cookie = null;

    public RecommenderAccess(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void logIn(long id){
        try {
            doGet(baseUrl+"login.do?userId="+id);
        } catch (IOException ex) {
            Logger.getLogger(RecommenderAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void view(String id, int docNr) {
        try {
            doGet(baseUrl+"view.do?itemId=" + id + "&docNr=" + docNr);
        } catch (IOException ex) {
            Logger.getLogger(RecommenderAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public NewsItemCluster[] getRecommendations() {
        try {
            String res = doGet(baseUrl+"GetRecommendations.do?count=250&start=0");
            GsonBuilder b = new GsonBuilder();
            b.registerTypeAdapter(NewsItemCluster.class, new CustomDeserializer());
            Gson gson = b.create();
            NewsItemCluster[] results = gson.fromJson(res, NewsItemCluster[].class);
            
            return results;
        } catch (IOException ex) {
            Logger.getLogger(RecommenderAccess.class.getName()).log(Level.SEVERE, null, ex);
            return new NewsItemCluster[]{};
        }
    }

    private String doGet(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        
        if (cookie == null){
            cookie = conn.getHeaderField("Set-Cookie");
        }else{
            conn.setRequestProperty("Cookie", cookie);
        }
        InputStream is = conn.getInputStream();
        BufferedInputStream b = new BufferedInputStream(is);
        byte[] content = IOUtils.toByteArray(b);
        return new String(content);
    }
}
