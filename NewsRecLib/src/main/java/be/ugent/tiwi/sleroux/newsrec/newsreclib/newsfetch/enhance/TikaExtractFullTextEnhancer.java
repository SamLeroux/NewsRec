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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsfetch.enhance;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TikaExtractFullTextEnhancer implements IEnhancer {

    private static final Logger logger = Logger.getLogger(TikaExtractFullTextEnhancer.class);
    private static final ResourceBundle bundle = PropertyResourceBundle.getBundle("newsRec");

    @Override
    public void enhance(NewsItem item) throws EnhanceException {
        logger.debug("start tika enhancement for " + item.getUrl());
        InputStream in = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) item.getUrl().openConnection();
            HttpURLConnection.setFollowRedirects(true);
            urlConnection.setRequestProperty("User-Agent", bundle.getString("useragent"));
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] content = IOUtils.toByteArray(in);
            urlConnection.disconnect();

            Tika tika = new Tika();
            tika.setMaxStringLength(Integer.MAX_VALUE);
            String mimeType = tika.detect(content);

            Metadata metadata = new Metadata();
            BodyContentHandler ch = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();

            metadata.set(Metadata.CONTENT_TYPE, mimeType);
            parser.parse(new ByteArrayInputStream(content), ch, metadata, new ParseContext());

            BodyContentHandler textHandler = new BodyContentHandler(-1);
            BoilerpipeContentHandler boilerpipeHandler = new BoilerpipeContentHandler(textHandler);

            parser.parse(new ByteArrayInputStream(content), boilerpipeHandler, metadata, new ParseContext());

            item.setFulltext(boilerpipeHandler.getTextDocument().getContent());
            LanguageIdentifier identifier = new LanguageIdentifier(item.getFulltext());
            item.setLocale(new Locale(identifier.getLanguage()));

        } catch (IOException | SAXException | TikaException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

}
