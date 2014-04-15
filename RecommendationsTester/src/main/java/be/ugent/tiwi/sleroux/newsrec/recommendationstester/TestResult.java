/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TestResult {
    private int results;
    private int relevantResults;
    private int relevantResultsNotYetSeen;
    private int neededTime;
    private double trendingResults;
    private double personalResults;

    public TestResult(int results, int relevantResults, int relevantResultsNotYetSeen, int neededTime, double trendingResults, double personalResults) {
        this.results = results;
        this.relevantResults = relevantResults;
        this.relevantResultsNotYetSeen = relevantResultsNotYetSeen;
        this.neededTime = neededTime;
        this.trendingResults = trendingResults;
        this.personalResults = personalResults;
    }

    public double getTrendingResults() {
        return trendingResults;
    }

    public void setTrendingResults(double trendingResults) {
        this.trendingResults = trendingResults;
    }

    public double getPersonalResults() {
        return personalResults;
    }

    public void setPersonalResults(double personalResults) {
        this.personalResults = personalResults;
    }

    

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public int getRelevantResults() {
        return relevantResults;
    }

    public void setRelevantResults(int relevantResults) {
        this.relevantResults = relevantResults;
    }

    public int getRelevantResultsNotYetSeen() {
        return relevantResultsNotYetSeen;
    }

    public void setRelevantResultsNotYetSeen(int relevantResultsNotYetSeen) {
        this.relevantResultsNotYetSeen = relevantResultsNotYetSeen;
    }

    public int getNeededTime() {
        return neededTime;
    }

    public void setNeededTime(int neededTime) {
        this.neededTime = neededTime;
    }
    
    
}
