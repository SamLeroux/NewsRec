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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Represents an article.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsItem {

    private long id;
    private int docNr;
    private String title;
    private List<String> authors;
    private String fulltext;
    private String description;
    private Date timestamp;
    private Map<String, Float> terms;
    private Locale locale;
    private String source;
    
    private URL url;
    private URL imageUrl;

    /**
     *
     */
    public NewsItem() {
        terms = new HashMap<>();
        authors = new ArrayList<>();
        id = Math.abs(UUID.randomUUID().getMostSignificantBits());
    }

    /**
     *
     * @param docNr
     * @param title
     * @param authors
     * @param fulltext
     * @param description
     * @param timestamp
     * @param terms
     * @param locale
     * @param source
     * @param url
     * @param imageUrl
     */
    public NewsItem(int docNr,String title, List<String> authors,String fulltext, String description, Date timestamp, Map<String, Float> terms, Locale locale, String source, URL url, URL imageUrl) {
        this(Math.abs(UUID.randomUUID().getMostSignificantBits()), docNr, title, authors, fulltext, description, timestamp, terms, locale, source, url, imageUrl);
    }

    /**
     *
     * @param id
     * @param docNr
     * @param title
     * @param authors
     * @param fulltext
     * @param description
     * @param timestamp
     * @param terms
     * @param locale
     * @param source
     * @param url
     * @param imageUrl
     */
    public NewsItem(long id, int docNr, String title, List<String> authors, String fulltext, String description, Date timestamp, Map<String, Float> terms, Locale locale, String source, URL url, URL imageUrl) {
        this.id = id;
        this.docNr = docNr;
        this.title = title;
        this.authors = authors;
        this.fulltext = fulltext;
        this.description = description;
        this.timestamp = timestamp;
        this.terms = terms;
        this.locale = locale;
        this.source = source;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    /**
     *
     * @return
     */
    public URL getImageUrl() {
        return imageUrl;
    }

    /**
     *
     * @param imageUrl
     */
    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     *
     * @return
     */
    public URL getUrl() {
        return url;
    }

    /**
     *
     * @param url
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     */
    public String[] getAuthors() {
        return authors.toArray(new String[authors.size()]);
    }

    /**
     *
     * @param author
     */
    public void addAuthor(String author) {
        authors.add(author);
    }

    /**
     *
     * @return
     */
    public String getFulltext() {
        return fulltext;
    }

    /**
     *
     * @param fulltext
     */
    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }

    /**
     *
     * @return
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @return
     */
    public Map<String, Float> getTerms() {
        return terms;
    }

    /**
     *
     * @param terms
     */
    public void setTerms(Map<String, Float> terms) {
        this.terms = terms;
    }

    /**
     *
     * @return
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     *
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
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
    public String getSource() {
        return source;
    }

    /**
     *
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     *
     * @param term
     * @param relevancy
     */
    public void addTerm(String term, float relevancy){
        terms.put(term, relevancy);
    }

    /**
     *
     * @return
     */
    public int getDocNr() {
        return docNr;
    }

    /**
     *
     * @param docNr
     */
    public void setDocNr(int docNr) {
        this.docNr = docNr;
    }
    
    

    @Override
    public String toString() {
        return "NewsItem{" + "id=" + id + ", title=" + title + ", authors=" + authors + ", fulltext=" + fulltext + ", description=" + description + ", timestamp=" + timestamp + ", terms=" + terms + ", locale=" + locale + ", source=" + source + ", url=" + url + '}';
    }
    
    

}
