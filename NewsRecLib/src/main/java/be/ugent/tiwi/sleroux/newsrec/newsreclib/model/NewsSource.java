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

package be.ugent.tiwi.sleroux.newsrec.newsreclib.model;

import java.util.Date;

/**
 * Represents a news source such as a newspaper.
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsSource {
    private String name;
    private Date lastFetched;
    private int fetchinterval;
    private String description;
    protected String rssUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(Date lastFetched) {
        this.lastFetched = lastFetched;
    }

    public int getFetchinterval() {
        return fetchinterval;
    }

    public void setFetchinterval(int fetchinterval) {
        this.fetchinterval = fetchinterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }
    
    
}
