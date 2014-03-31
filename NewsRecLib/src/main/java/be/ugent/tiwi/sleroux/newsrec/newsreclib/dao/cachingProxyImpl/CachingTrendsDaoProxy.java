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

package be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.cachingProxyImpl;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class CachingTrendsDaoProxy implements ITrendsDao{

    private final ITrendsDao inner;
    private String[] trends = null;
    private int cnt = 0;
    private static final Logger logger = Logger.getLogger(CachingTrendsDaoProxy.class);
    

    public CachingTrendsDaoProxy(ITrendsDao inner) {
        this.inner = inner;
    }
    
    
    @Override
    public String[] getTrends() throws TrendsDaoException {
        if (cnt == 0 || trends == null){
            logger.info("requesting trends from inner dao");
            trends = inner.getTrends();
        }
        else{
            logger.info("returning cached trends");
        }
        cnt++;
        cnt = cnt % 10;
        return trends;
    }

    @Override
    public void updateTrends(String[] trends) throws TrendsDaoException {
        inner.updateTrends(trends);
        cnt = 0;
    }

    @Override
    public void close() throws DaoException {
        inner.close();
    }

    @Override
    public String[] getTrends(int n) throws TrendsDaoException {
        return inner.getTrends(n);
    }
    
}
    