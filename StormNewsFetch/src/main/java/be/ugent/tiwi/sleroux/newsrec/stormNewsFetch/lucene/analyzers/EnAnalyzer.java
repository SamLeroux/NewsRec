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
package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.lucene.analyzers;

import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.config.Config;
import java.io.Reader;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.tartarus.snowball.ext.EnglishStemmer;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class EnAnalyzer extends Analyzer {

    private static final Logger logger = Logger.getLogger(EnAnalyzer.class);
    private CharArraySet stopwords = null;
    private SynonymMap synonyms = null;

    /**
     *
     */
    public EnAnalyzer() {
    }

    /**
     *
     * @param stopwords
     */
    public EnAnalyzer(CharArraySet stopwords) {
        this.stopwords = stopwords;
    }

    /**
     *
     * @param synonyms
     */
    public EnAnalyzer(SynonymMap synonyms) {
        this.synonyms = synonyms;
    }

    /**
     *
     * @param stopwords
     * @param synonyms
     */
    public EnAnalyzer(CharArraySet stopwords, SynonymMap synonyms) {
        this.stopwords = stopwords;
        this.synonyms = synonyms;
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
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        //reader = new HTMLStripCharFilter(reader);
        Tokenizer t = new StandardTokenizer(Config.LUCENE_VERSION, reader);
        TokenStream result = t;
        
        //result = new SynonymFilter(result, synonyms, true);
        result = new StandardFilter(Config.LUCENE_VERSION, result);
        result = new LowerCaseFilter(Config.LUCENE_VERSION, result);
        result = new TrimFilter(Config.LUCENE_VERSION, result);
        result = new ASCIIFoldingFilter(result);
        if (stopwords != null) {
            result = new StopFilter(Config.LUCENE_VERSION, result, stopwords);
        } else {
            logger.info("No stopwordsfile provided, no stopword removal");
        }
        //result = new LowerCaseFilter(Version.LUCENE_46, result);
        result = new EnglishPossessiveFilter(Config.LUCENE_VERSION, result);
        //result = new PorterStemFilter(result);
        result = new SnowballFilter(result, new EnglishStemmer());
//        ShingleFilter sf = new ShingleFilter(result, 2, 3);
//        sf.setFillerToken(null);
//        result = sf;
        TokenStreamComponents comp = new TokenStreamComponents(t, result);
        return comp;
    }

}
