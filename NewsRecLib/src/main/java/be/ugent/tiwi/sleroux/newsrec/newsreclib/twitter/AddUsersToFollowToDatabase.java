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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.twitter;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITwitterFollowersDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.MysqlFollowersDao;
import org.apache.log4j.Logger;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class AddUsersToFollowToDatabase {

    private static final String[] users = new String[]{"BillGates", "TheEconomist", "WhiteHouse", "nytimes", "BBCBreaking", "ForbesTech", "CNN", "Forbes", "cnntech", "HuffingtonPost", "guardiantech", "washingtonpost", "BBCWorld", "New_Europe"};
    private static Twitter twitter;
    private static final Logger logger = Logger.getLogger(AddUsersToFollowToDatabase.class);
    private static ITwitterFollowersDao dao;

    public static void main(String[] args) throws DaoException {
        dao = new MysqlFollowersDao();
        //insert();
        for (long l : dao.getUsersToFollow()) {
            System.out.println(l);
        }
    }

    public static void insert() throws DaoException {
        twitter = TwitterFactory.getSingleton();

        AccessToken accessToken = new AccessToken(TwitterCredentialProvider.getAccessToken(), TwitterCredentialProvider.getAccessSecret());
        twitter.setOAuthConsumer(TwitterCredentialProvider.get0AuthConsumerKey(), TwitterCredentialProvider.get0AuthConsumerSecret());
        twitter.setOAuthAccessToken(accessToken);

        for (String user : users) {
            try {
                long id = getId(user);
                dao.addUserToFollow(user, id);
            } catch (TwitterException ex) {
                logger.error(ex);
            }

        }
    }

    public static long getId(String username) throws TwitterException {
        return twitter.showUser(username).getId();
    }
}
