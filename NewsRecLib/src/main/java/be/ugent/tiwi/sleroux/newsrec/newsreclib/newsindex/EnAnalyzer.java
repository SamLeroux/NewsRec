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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsindex;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.config.Config;
import java.io.Reader;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class EnAnalyzer extends Analyzer {

    private static final Logger logger = Logger.getLogger(EnAnalyzer.class);
    private CharArraySet stopwords = null;
    private SynonymMap synonyms = null;

    public EnAnalyzer() {
    }

    public EnAnalyzer(CharArraySet stopwords) {
        this.stopwords = stopwords;
    }

    public EnAnalyzer(SynonymMap synonyms) {
        this.synonyms = synonyms;
    }

    public EnAnalyzer(CharArraySet stopwords, SynonymMap synonyms) {
        this.stopwords = stopwords;
        this.synonyms = synonyms;
    }

    public CharArraySet getStopwords() {
        return stopwords;
    }

    public void setStopwords(CharArraySet stopwords) {
        this.stopwords = stopwords;
    }

    public SynonymMap getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(SynonymMap synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer t = new StandardTokenizer(Config.LUCENE_VERSION, reader);
        TokenStream result = t;
        //result = new SynonymFilter(result, synonyms, true);
        result = new LowerCaseFilter(Config.LUCENE_VERSION, result);
        result = new ASCIIFoldingFilter(result);
        if (stopwords != null) {
            result = new StopFilter(Config.LUCENE_VERSION, result, stopwords);
        } else {
            logger.info("No stopwordsfile provided, no stopword removal");
        }
        //result = new LowerCaseFilter(Version.LUCENE_46, result);
        result = new EnglishPossessiveFilter(Config.LUCENE_VERSION, result);
        result = new PorterStemFilter(result);


        TokenStreamComponents comp = new TokenStreamComponents(t, result);
        return comp;
    }

}
