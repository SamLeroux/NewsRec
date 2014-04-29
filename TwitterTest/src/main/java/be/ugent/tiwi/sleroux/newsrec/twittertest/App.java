/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.twittertest;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class App {

    public static void main(String[] args) throws TwitterException {
        Twitter twitter = TwitterFactory.getSingleton();
        AccessToken accessToken = new AccessToken("2369203675-1Pk99eotzhCznmIgr3iXb670DpsEVQCANuoBHRs", "6ua72cabIzcFsBgE15RVzULeKT44TnIrOIaf9chY9dlWe");
        twitter.setOAuthConsumer("tQjT8XvB7OPNTl8qdhchDo3J2", "FXWVS3OEW7omiUDSLpET0aRInoUumGPWRxOVyk7GrhiwcfLBnV");
        twitter.setOAuthAccessToken(accessToken);
        
        User user = twitter.showUser("billclinton");
        System.out.println("id=" + user.getId());
        StreamReaderService srs = new StreamReaderService();
        srs.readTwitterFeed();
    }
}
