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
public class App {
    public static void main(String[] args) {
        //RecommenderAccess ra = new RecommenderAccess("http://wicaweb5.intec.ugent.be:8080/");
        RecommenderAccess ra = new RecommenderAccess("http://localhost:8080/");
        TestingAgent ta = new TestingAgent(ra, 5, new String[]{"linux","android","google","microsoft","smartphone"});
        ta.start();        
    }
}
