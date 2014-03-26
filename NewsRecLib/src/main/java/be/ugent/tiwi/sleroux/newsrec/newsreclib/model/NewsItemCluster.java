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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsItemCluster {

    private List<NewsItem> items;
    private NewsItem representative;

    public NewsItemCluster() {
        items = new ArrayList<>();
    }

    public void addItem(NewsItem item) {
        if (representative == null || representative.getTimestamp().before(item.getTimestamp())) {
            if (representative != null){
                items.add(representative);
            }
            representative = item;
        } else {
            items.add(item);
        }
    }
    
    public int getSize(){
        return items.size();
    }

    public List<NewsItem> getItems() {
        return items;
    }

    public NewsItem getRepresentative() {
        return representative;
    }

    public void setRepresentative(NewsItem representative) {
        this.representative = representative;
    }

    public void setItems(List<NewsItem> items) {
        this.items = items;
    }
    
    
    
    
    

}
