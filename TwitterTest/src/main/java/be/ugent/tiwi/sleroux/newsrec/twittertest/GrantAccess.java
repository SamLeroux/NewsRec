/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.twittertest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class GrantAccess {

    public static void main(String args[]) throws Exception {
        // The factory instance is re-useable and thread safe.
        Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer("tQjT8XvB7OPNTl8qdhchDo3J2", "FXWVS3OEW7omiUDSLpET0aRInoUumGPWRxOVyk7GrhiwcfLBnV");
        
        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while (null == accessToken) {
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
                    te.printStackTrace();
                }
            }
        }
        storeAccessToken(twitter.verifyCredentials().getId(), accessToken);
    }

    private static void storeAccessToken(long useId, AccessToken accessToken) {
        System.out.println("AccessToken= "+ accessToken.getToken());
        System.out.println("TokenSecret= "+ accessToken.getTokenSecret());
    }
}
