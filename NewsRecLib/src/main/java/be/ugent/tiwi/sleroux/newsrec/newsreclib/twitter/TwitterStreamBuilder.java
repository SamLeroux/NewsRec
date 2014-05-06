package be.ugent.tiwi.sleroux.newsrec.newsreclib.twitter;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TwitterStreamBuilder {

    public static TwitterStream getStream() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(TwitterCredentialProvider.get0AuthConsumerKey());
        cb.setOAuthConsumerSecret(TwitterCredentialProvider.get0AuthConsumerSecret());
        cb.setOAuthAccessToken(TwitterCredentialProvider.getAccessToken());
        cb.setOAuthAccessTokenSecret(TwitterCredentialProvider.getAccessSecret());

        return new TwitterStreamFactory(cb.build()).getInstance();
    }
}
