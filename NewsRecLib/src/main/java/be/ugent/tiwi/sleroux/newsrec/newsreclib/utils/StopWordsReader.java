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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.config.Config;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Provides an easy way to read a stopwords file.
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class StopWordsReader {

    private static final Logger logger = Logger.getLogger(StopWordsReader.class);

    public static CharArraySet getStopwords(String stopwordsLocation) throws FileNotFoundException, IOException {
        logger.info("reading stopwords file: " + stopwordsLocation);
        CharArraySet stopw;
        try (BufferedReader reader = new BufferedReader(new FileReader(stopwordsLocation))) {
            stopw = new CharArraySet(Config.LUCENE_VERSION, 1000, true);
            String line = reader.readLine();
            while (line != null) {
                stopw.add(line);
                line = reader.readLine();
            }
        }
        return stopw;
    }
}
