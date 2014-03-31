/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TopTermTester {

    private final LuceneTopTermExtract extractor;
    private final IndexReader reader;

    public TopTermTester(LuceneTopTermExtract extractor, IndexReader reader) {
        this.extractor = extractor;
        this.reader = reader;
    }

    public void test(String id) {
        final Map<String, Double>  terms = extractor.getTopTerms(id, reader,100);
        SortedSet<String> s = new TreeSet<>(new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return terms.get(o2).compareTo(terms.get(o1));
            }
            
        });
        s.addAll(terms.keySet());
        for (String term : s) {
            System.out.println(term + ";" + terms.get(term));
        }
    }
}
