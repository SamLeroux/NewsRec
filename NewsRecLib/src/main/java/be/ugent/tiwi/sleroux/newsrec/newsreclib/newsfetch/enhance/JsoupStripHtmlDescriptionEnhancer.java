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
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class JsoupStripHtmlDescriptionEnhancer implements IEnhancer{

    private static final Logger logger = Logger.getLogger(JsoupStripHtmlDescriptionEnhancer.class);
    
    @Override
    public void enhance(NewsItem item) throws EnhanceException {
        logger.debug("start jsoup description enhancer");
        String description = item.getDescription();
        if (description != null){
            Document doc = Jsoup.parse(description);
            item.setDescription(doc.text());
        }
    }
    
}
