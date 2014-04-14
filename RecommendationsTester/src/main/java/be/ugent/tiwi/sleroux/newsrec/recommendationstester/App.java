/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import java.io.IOException;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        //RecommenderAccess ra = new RecommenderAccess("http://wicaweb5.intec.ugent.be:8080/");
        RecommenderAccess ra = new RecommenderAccess("http://localhost:8080/WebNewsRecommender/");
        TestingManager manager = new TestingManager("/home/sam/tester.conf", "/home/sam/tester.csv", ra, 100);
        manager.start();
//        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("/home/sam/Bureaublad/index")));
//        CharArraySet stopw = StopWordsReader.getStopwords("/home/sam/Bureaublad/dev/stopwords_EN.txt");
//        LuceneTopTermExtract termExtract = new LuceneTopTermExtract(new EnAnalyzer(stopw));
//        TopTermTester tester = new TopTermTester(termExtract, reader);
//        tester.test("4887519953487884911");
    }
}
