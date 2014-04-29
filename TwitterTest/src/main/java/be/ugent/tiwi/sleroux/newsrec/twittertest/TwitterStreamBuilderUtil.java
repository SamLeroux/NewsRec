package be.ugent.tiwi.sleroux.newsrec.twittertest;

import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TwitterStreamBuilderUtil {

    public static TwitterStream getStream() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("tQjT8XvB7OPNTl8qdhchDo3J2");
        cb.setOAuthConsumerSecret("FXWVS3OEW7omiUDSLpET0aRInoUumGPWRxOVyk7GrhiwcfLBnV");
        cb.setOAuthAccessToken("2369203675-1Pk99eotzhCznmIgr3iXb670DpsEVQCANuoBHRs");
        cb.setOAuthAccessTokenSecret("6ua72cabIzcFsBgE15RVzULeKT44TnIrOIaf9chY9dlWe");

        
        return new TwitterStreamFactory(cb.build()).getInstance();
    }
}
