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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.LanguageAnalyzerHelper;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.NewsRecLuceneAnalyzer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology.StreamIDs;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.tika.language.LanguageIdentifier;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TweetAnalyzerBolt extends BaseRichBolt {

    private OutputCollector collector;
    private static final Logger logger = Logger.getLogger(TweetAnalyzerBolt.class);
    private static final Pattern PATTERN = Pattern.compile("([A-Z][a-z]+( [A-Z][a-z]+)+)");

    public TweetAnalyzerBolt() {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StreamIDs.TERMSTREAM, new Fields(StreamIDs.TERM));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        try {
            String tweet = (String) input.getValueByField(StreamIDs.TWEET);
            Reader reader = new StringReader(tweet);

            LanguageIdentifier identifier = new LanguageIdentifier(tweet);
            NewsRecLuceneAnalyzer analyzer = LanguageAnalyzerHelper.getInstance().getAnalyzer(new Locale(identifier.getLanguage()));

            TokenStream tokenStream = analyzer.tokenStream("", reader);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                collector.emit(StreamIDs.TERMSTREAM, new Values(term));
            }
            reader.close();
            tokenStream.close();

            for (String term : extractNames(tweet, analyzer.getStopwords())) {
                collector.emit(StreamIDs.TERMSTREAM, new Values(term));
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    private List<String> extractNames(String tweet, CharArraySet stopwords) {
        Matcher m = PATTERN.matcher(tweet);

        List<String> results = new ArrayList<>();
        while (m.find()) {
            String term = m.group(1).toLowerCase();
            boolean stop = false;
            int i = 0;
            String[] comp = term.split(" ");
            while (i < comp.length && !stop) {
                stop = stopwords.contains(comp[i]);
                i++;
            }
            if (!stopwords.contains(term) && !stop) {
                results.add(term);
            }
        }
        return results;
    }

}
