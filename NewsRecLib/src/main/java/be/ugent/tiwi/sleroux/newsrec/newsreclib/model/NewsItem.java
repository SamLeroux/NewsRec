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
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an article.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsItem {

    /**
     *The id is a numeric identification (long) but is stored as a String.
     * There were some problems retrieving documents from the lucene index based
     * on the numeric value.
     */
    private String id;
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

    public NewsItem() {
        terms = new HashMap<>();
        authors = new ArrayList<>();
        id = Long.toString(Math.abs(UUID.randomUUID().getLeastSignificantBits()));
    }

    public NewsItem(int docNr, String title, List<String> authors, String fulltext, String description, Date timestamp, Map<String, Float> terms, Locale locale, String source, URL url, URL imageUrl) {
        this(Long.toString(Math.abs(UUID.randomUUID().getLeastSignificantBits())), docNr, title, authors, fulltext, description, timestamp, terms, locale, source, url, imageUrl);
    }

    public NewsItem(String id, int docNr, String title, List<String> authors, String fulltext, String description, Date timestamp, Map<String, Float> terms, Locale locale, String source, URL url, URL imageUrl) {
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

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Map<String, Float> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, Float> terms) {
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void addTerm(String term, float relevancy) {
        terms.put(term, relevancy);
    }

    public int getDocNr() {
        return docNr;
    }

    public void setDocNr(int docNr) {
        this.docNr = docNr;
    }

    @Override
    public String toString() {
        return "NewsItem{" + "id=" + id + ", title=" + title + ", authors=" + authors + ", fulltext=" + fulltext + ", description=" + description + ", timestamp=" + timestamp + ", terms=" + terms + ", locale=" + locale + ", source=" + source + ", url=" + url + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.title);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NewsItem other = (NewsItem) obj;
        return Objects.equals(this.title, other.title);
    }
    
    

}
