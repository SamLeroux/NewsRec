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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.utils;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import com.google.gson.Gson;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

/**
 * Converts NewsItems to Lucene documents and back.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 * @see NewsItem
 * @see Document
 */
public class NewsItemLuceneDocConverter {
    private static Gson gson = new Gson();

    public static Document NewsItemToDocument(NewsItem item) {
        Document doc = new Document();
        FieldType ftype = getTextType();

        if (item.getTitle() != null) {
            doc.add(new Field("title", item.getTitle(), ftype));
        }
        if (item.getFulltext() != null) {
            doc.add(new Field("text", item.getFulltext(), ftype));
        }
        if (item.getDescription() != null) {
            doc.add(new Field("description", item.getDescription(), ftype));
        }

        doc.add(new StringField("id", item.getId(), Field.Store.YES));

        if (item.getUrl() != null) {
            doc.add(new StringField("url", item.getUrl().toString(), Field.Store.YES));
        }
        if (item.getImageUrl() != null) {
            doc.add(new StringField("imageUrl", item.getImageUrl().toString(), Field.Store.YES));
        }
        if (item.getLocale() != null) {
            doc.add(new StringField("locale", item.getLocale().getISO3Language(), Field.Store.YES));
        }
        if (item.getSource() != null) {
            doc.add(new StringField("source", item.getSource(), Field.Store.YES));
        }
        if (item.getTimestamp() != null) {
            doc.add(new LongField("timestamp", item.getTimestamp().getTime(), Field.Store.YES));
        }
        for (String author : item.getAuthors()) {
            doc.add(new StringField("author", author, Field.Store.YES));
        }
        
        Map<String, Double> terms = item.getTerms();
        String termsJson = gson.toJson(terms);
        doc.add(new StoredField("terms", termsJson));
        
        return doc;
    }

    public static NewsItem DocumentToNewsItem(Document d) {
        NewsItem item = new NewsItem();
        IndexableField field;

        field = d.getField("description");
        if (field != null) {
            item.setDescription(field.stringValue());
        } else {
            item.setDescription("No description available");
        }

        field = d.getField("source");
        if (field != null) {
            item.setSource(field.stringValue());
        } else {
            item.setSource("No source available");
        }

        field = d.getField("text");
        if (field != null) {
            item.setFulltext(field.stringValue());
        } else {
            item.setFulltext("No text available");
        }

        field = d.getField("id");
        if (field != null) {
            item.setId(field.stringValue());
        } else {
            item.setId("");
        }

        field = d.getField("imageUrl");
        if (field != null) {
            try {
                item.setImageUrl(new URL(field.stringValue()));
            } catch (MalformedURLException ex) {
                item.setImageUrl(null);
            }
        }

        field = d.getField("locale");
        if (field != null) {
            item.setLocale(Locale.forLanguageTag(field.stringValue()));
        } else {
            item.setLocale(Locale.getDefault());
        }

        field = d.getField("timestamp");
        if (field != null) {
            item.setTimestamp(new Date(field.numericValue().longValue()));
        } else {
            item.setTimestamp(new Date());
        }

        field = d.getField("title");
        if (field != null) {
            item.setTitle(field.stringValue());
        } else {
            item.setTitle("");
        }

        field = d.getField("url");
        if (field != null) {
            try {
                item.setUrl(new URL(field.stringValue()));
            } catch (MalformedURLException ex) {
                item.setUrl(null);
            }
        } else {
            item.setTitle("");
        }

        field = d.getField("terms");
        if (field != null){
            Map<String, Double> terms = gson.fromJson(field.stringValue(), HashMap.class);
            item.addTerms(terms);
        }

        return item;
    }

    private static FieldType getTextType() {
        FieldType ftype = new FieldType();
        ftype.setIndexed(true);
        ftype.setStoreTermVectors(true);
        ftype.setStored(true);
        ftype.freeze();
        return ftype;
    }
}
