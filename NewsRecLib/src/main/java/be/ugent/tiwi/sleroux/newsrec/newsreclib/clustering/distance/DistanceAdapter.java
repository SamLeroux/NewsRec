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

package be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.distance;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import com.aliasi.util.Distance;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DistanceAdapter implements Distance{

    private final IDistance distance;

    public DistanceAdapter(IDistance distance) {
        this.distance = distance;
    }
    
    
    @Override
    public double distance(Object e, Object e1) {
        NewsItem n1 = (NewsItem)e;
        NewsItem n2 = (NewsItem)e1;
        
        return distance.distance(n1, n2);
    }
    
}
