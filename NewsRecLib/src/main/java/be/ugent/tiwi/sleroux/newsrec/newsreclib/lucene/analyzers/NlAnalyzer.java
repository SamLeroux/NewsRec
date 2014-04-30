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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.config.Config;
import static be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.NewsRecLuceneAnalyzer.logger;
import java.io.Reader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tartarus.snowball.ext.DutchStemmer;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NlAnalyzer extends NewsRecLuceneAnalyzer{

    @Override
    protected TokenStreamComponents createComponents(Reader reader) {
        Tokenizer t = new StandardTokenizer(Config.LUCENE_VERSION, reader);
        TokenStream result = t;

        result = new StandardFilter(Config.LUCENE_VERSION, result);
        result = new LowerCaseFilter(Config.LUCENE_VERSION, result);
        result = new TrimFilter(Config.LUCENE_VERSION, result);
        result = new ASCIIFoldingFilter(result);
        if (stopwords != null) {
            result = new StopFilter(Config.LUCENE_VERSION, result, stopwords);
        } else {
            logger.info("No stopwordsfile provided, no stopword removal");
        }
     
        result = new SnowballFilter(result, new DutchStemmer());

        TokenStreamComponents comp = new TokenStreamComponents(t, result);
        return comp;
    }
    
}
