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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class UserHelper {

    private final Twitter twitter;
    private static final Logger logger = Logger.getLogger(UserHelper.class);

    public UserHelper() {
        twitter = TwitterFactory.getSingleton();
        AccessToken accessToken = new AccessToken(TwitterCredentialProvider.getAccessToken(), TwitterCredentialProvider.getAccessSecret());
        twitter.setOAuthConsumer(TwitterCredentialProvider.get0AuthConsumerKey(), TwitterCredentialProvider.get0AuthConsumerSecret());
        twitter.setOAuthAccessToken(accessToken);

    }

    public long getUserId(String screenName) throws TwitterException {
        User user = twitter.showUser(screenName);
        return user.getId();
    }

    public void grantAccess() throws TwitterException {
        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (null == accessToken) {
            try {
                System.out.println("Open the following URL and grant access to your account:");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
                String pin = br.readLine();

                try {
                    if (pin.length() > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    } else {
                        accessToken = twitter.getOAuthAccessToken();
                    }
                } catch (TwitterException te) {
                    if (401 == te.getStatusCode()) {
                        System.out.println("Unable to get the access token.");
                    } else {
                        logger.error(te);
                    }
                }
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        storeAccessToken(twitter.verifyCredentials().getId(), accessToken);
    }

    private static void storeAccessToken(long useId, AccessToken accessToken) {
        System.out.println("Userid=" + useId);
        System.out.println("AccessToken= " + accessToken.getToken());
        System.out.println("TokenSecret= " + accessToken.getTokenSecret());
    }
}
