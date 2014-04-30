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

import java.io.Reader;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public abstract class NewsRecLuceneAnalyzer extends Analyzer {
    protected static final Logger logger = Logger.getLogger(EnAnalyzer.class);
    protected CharArraySet stopwords = null;
    protected SynonymMap synonyms = null;

    public NewsRecLuceneAnalyzer() {
    }

    /**
     *
     * @return
     */
    public CharArraySet getStopwords() {
        return stopwords;
    }

    /**
     *
     * @param stopwords
     */
    public void setStopwords(CharArraySet stopwords) {
        this.stopwords = stopwords;
    }

    /**
     *
     * @return
     */
    public SynonymMap getSynonyms() {
        return synonyms;
    }

    /**
     *
     * @param synonyms
     */
    public void setSynonyms(SynonymMap synonyms) {
        this.synonyms = synonyms;
    }

    /**
     *
     * @param fieldName
     * @param reader
     * @return
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader){
        return createComponents(reader);
    }
    
    protected abstract TokenStreamComponents createComponents(Reader reader);
    
}
