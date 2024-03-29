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

package be.ugent.tiwi.sleroux.newsrec.stormNewsFetch.model;

import java.util.Map;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsConsumer {
    private int id;
    private String username;
    private String passwordHash;
    private Map<String, Double> interests;

    /**
     *
     */
    public NewsConsumer() {
    }

    /**
     *
     * @param id
     * @param username
     * @param passwordHash
     * @param interests
     */
    public NewsConsumer(int id, String username, String passwordHash, Map<String, Double> interests) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.interests = interests;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     *
     * @param passwordHash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     *
     * @return
     */
    public Map<String, Double> getInterests() {
        return interests;
    }

    /**
     *
     * @param interests
     */
    public void setInterests(Map<String, Double> interests) {
        this.interests = interests;
    }
    
    
}
