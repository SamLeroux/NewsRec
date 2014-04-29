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

import java.util.PropertyResourceBundle;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TwitterCredentialProvider {

    private static final PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle("twitter");

    public static String get0AuthConsumerKey(){
        return bundle.getString("0AuthConsumerkey");
    }
    
    public static String get0AuthConsumerSecret(){
        return bundle.getString("0AuthConsumerSecret");
    }
    
    public static String getAccessToken(){
        return bundle.getString("0AuthAccessToken");
    }
    
    public static String getAccessSecret(){
        return bundle.getString("0AuthTokenSecret");
    }
}
