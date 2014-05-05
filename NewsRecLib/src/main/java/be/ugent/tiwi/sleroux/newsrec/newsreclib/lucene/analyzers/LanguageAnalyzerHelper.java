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

package be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.StopWordsReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LanguageAnalyzerHelper {

    private final Map<Locale, NewsRecLuceneAnalyzer> analyzerMap;
    private static LanguageAnalyzerHelper instance;
    private static final Logger logger = Logger.getLogger(LanguageAnalyzerHelper.class);
    
    private LanguageAnalyzerHelper() {
        
        analyzerMap = new HashMap<>();
        PropertyResourceBundle b = (PropertyResourceBundle) PropertyResourceBundle.getBundle("newsRec");
        
        NewsRecLuceneAnalyzer a = new EnAnalyzer();
        try {
            a.setStopwords(StopWordsReader.getStopwords(b.getString("stopwEng")));
        } catch (IOException ex) {
            a.setStopwords(StopWordsReader.getDefaultStopwords());
        }
        analyzerMap.put(Locale.ENGLISH, a);
        
        NewsRecLuceneAnalyzer a2 = new EnAnalyzer();
        try {
            a2.setStopwords(StopWordsReader.getStopwords(b.getString("stopwNl")));
        } catch (IOException ex) {
            a2.setStopwords(StopWordsReader.getDefaultStopwords());
        }
        analyzerMap.put(Locale.forLanguageTag("nl"), a2);
    }
    
    public static LanguageAnalyzerHelper getInstance() {
        if (instance == null) {
            synchronized (LanguageAnalyzerHelper.class) {
                if (instance == null) {
                    instance = new LanguageAnalyzerHelper();
                }
            }
        }
        return instance;
    }
    
    public NewsRecLuceneAnalyzer getAnalyzer(Locale l) {
        if (!analyzerMap.containsKey(l)){
            logger.warn("No analyzer registered for locale: "+l);
            logger.info("returning default analyzer");
            return analyzerMap.get(Locale.ENGLISH);
        }
        return analyzerMap.get(l);
    }
    
}
