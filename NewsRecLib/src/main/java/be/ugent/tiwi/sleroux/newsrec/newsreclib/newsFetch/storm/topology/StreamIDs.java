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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm.topology;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class StreamIDs {

    /**
     *
     */
    public static final String NEWSSOURCESTREAM = "rssUrlStream";

    /**
     *
     */
    public static final String NEWSSOURCEITEM = "newsSource";

    /**
     *
     */
    public static final String NEWSARTICLENOCONTENTSTREAM = "articleNoContentStream";

    /**
     *
     */
    public static final String NEWSARTICLENOCONTENT = "articleNoContent";

    /**
     *
     */
    public static final String NEWSARTICLESOURCE = "sourcename";

    /**
     *
     */
    public static final String UPDATEDNEWSSOURCESTREAM = "updatedNewsSourceStream";

    /**
     *
     */
    public static final String UPDATEDNEWSSOURCE = "updatedNewsSource";

    /**
     *
     */
    public static final String NEWSARTICLEWITHCONTENTSTREAM = "articleWithContentStream";

    /**
     *
     */
    public static final String NEWSARTICLEWITHCONTENT = "articleWithContent";

    /**
     *
     */
    public static final String INDEXEDITEMSTREAM = "indexedItemStream";

    /**
     *
     */
    public static final String INDEXEDITEM = "indexeditem";

    /**
     *
     */
    public static final String TERMSTREAM = "TermStream";

    /**
     *
     */
    public static final String TERM = "term";

    public static final String TWEET = "tweet";
    public static final String TWEETSTREAM = "tweetstream";

}
