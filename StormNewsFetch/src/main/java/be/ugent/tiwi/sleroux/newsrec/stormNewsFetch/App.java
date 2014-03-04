/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch;

import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.INewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.dummyImpl.DummyNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.dao.mysqlImpl.MysqlNewsSourceDao;
import be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.storm.topology.NewsFetchTopologyStarter;
import org.apache.log4j.Logger;


/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class App {
    public static void main(String[] args) {
        try {
            INewsSourceDao newsSourceDao = new MysqlNewsSourceDao();
            NewsFetchTopologyStarter starter = new NewsFetchTopologyStarter(newsSourceDao, "newsfetch");
            starter.start();
            Thread.sleep(500000);
            starter.stop();
            
        } catch (InterruptedException ex) {
            Logger.getLogger("main").error(ex);
        }
    }
}
