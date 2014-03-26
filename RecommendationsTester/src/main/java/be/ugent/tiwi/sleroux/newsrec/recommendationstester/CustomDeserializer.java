/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItemCluster;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class CustomDeserializer implements JsonDeserializer<NewsItemCluster> {

    @Override
    public NewsItemCluster deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jo = json.getAsJsonObject();
        NewsItem[] items = context.deserialize(jo.get("items"), NewsItem[].class);

        NewsItem rep = context.deserialize(jo.get("representative"), NewsItem.class);
        NewsItemCluster cluster = new NewsItemCluster();
        cluster.setItems(Arrays.asList(items));
        cluster.setRepresentative(rep);
        return cluster;
    }

}
