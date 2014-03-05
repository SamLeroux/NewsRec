package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.model;

/*
 * Copyright 2014 Sam Leroux <sam.leroux@ugent.be>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable;
import java.net.URL;
import java.util.Date;

/**
 * Represents a news source such as a newspaper.
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsSource implements Serializable{
    private int id;
    private String name;
    private Date lastArticleFetchTime;
    private Date lastFetchTry;
    private int fetchinterval;
    private String description;
    private URL rssUrl;

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The timestamp of the last seen article.
     * @return 
     */
    public Date getLastArticleFetchTime() {
        return lastArticleFetchTime;
    }

    /**
     *
     * @param lastArticleFetchTime
     */
    public void setLastArticleFetchTime(Date lastArticleFetchTime) {
        this.lastArticleFetchTime = lastArticleFetchTime;
    }

    /**
     * The timestamp of the last check for new articles.
     * @return 
     */
    public Date getLastFetchTry() {
        return lastFetchTry;
    }

    /**
     *
     * @param lastFetchTry
     */
    public void setLastFetchTry(Date lastFetchTry) {
        this.lastFetchTry = lastFetchTry;
    }

    

    /**
     * The time between checks for new articles in seconds
     * @return 
     */
    public int getFetchinterval() {
        return fetchinterval;
    }

    /**
     *
     * @param fetchinterval
     */
    public void setFetchinterval(int fetchinterval) {
        this.fetchinterval = fetchinterval;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public URL getRssUrl() {
        return rssUrl;
    }

    /**
     *
     * @param rssUrl
     */
    public void setRssUrl(URL rssUrl) {
        this.rssUrl = rssUrl;
    }

    @Override
    public String toString() {
        return "NewsSource{" + "id=" + id + ", name=" + name + ", lastArticleFetchTime=" + lastArticleFetchTime + ", lastFetchTry=" + lastFetchTry + ", fetchinterval=" + fetchinterval + ", description=" + description + ", rssUrl=" + rssUrl + '}';
    }

    
    
    
}
