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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Reprensents an article.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsItem {

    private int id;
    private String title;
    private List<String> authors;
    private String fulltext;
    private String description;
    private Date timestamp;
    private Map<String, Double> terms;
    private Locale locale;
    private NewsSource source;
    private URL url;

    public NewsItem() {
        terms = new HashMap<>();
        authors = new ArrayList<>();
    }

    public NewsItem(int id, String title, List<String> authors, String fulltext, String description, Date timestamp, Map<String, Double> terms, Locale locale, NewsSource source, URL url) {
        this();
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.fulltext = fulltext;
        this.description = description;
        this.timestamp = timestamp;
        this.terms = terms;
        this.locale = locale;
        this.source = source;
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getAuthors() {
        return authors.toArray(new String[authors.size()]);
    }

    public void addAuthor(String author) {
        authors.add(author);
    }

    public String getFulltext() {
        return fulltext;
    }

    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Double> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, Double> terms) {
        this.terms = terms;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NewsSource getSource() {
        return source;
    }

    public void setSource(NewsSource source) {
        this.source = source;
    }
    
    public void addTerm(String term, double relevancy){
        terms.put(term, relevancy);
    }

    @Override
    public String toString() {
        return "NewsItem{" + "id=" + id + ", title=" + title + ", authors=" + authors + ", fulltext=" + fulltext + ", description=" + description + ", timestamp=" + timestamp + ", terms=" + terms + ", locale=" + locale + ", source=" + source + ", url=" + url + '}';
    }
    
    

}
