/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.twittertest;

import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class StreamReaderService {

    public void readTwitterFeed() {

        TwitterStream stream = TwitterStreamBuilderUtil.getStream();

        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception e) {
            }

            @Override
            public void onTrackLimitationNotice(int n) {
            }

            @Override
            public void onStatus(Status status) {
                if (status.getLang().equals("en")){
                    System.out.println(status.getText());
                }
            }

            @Override
            public void onStallWarning(StallWarning arg0) {
            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
            }
        };

        stream.addListener(listener);
        FilterQuery f = new FilterQuery();
        f.language(new String[]{"en"});
        f.follow(new long[]{816653});
        stream.filter(f);
        
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ex) {
            Logger.getLogger(StreamReaderService.class.getName()).log(Level.SEVERE, null, ex);
        }
        stream.shutdown();
    }
}
