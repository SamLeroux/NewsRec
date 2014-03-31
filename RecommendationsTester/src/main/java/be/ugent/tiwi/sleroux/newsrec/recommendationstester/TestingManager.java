/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TestingManager {

    private final List<ProfileTestingAgent> agents;
    private FileWriter writer;
    private boolean stop = false;
    private static final Random rnd = new Random();

    public TestingManager(String input, String output, RecommenderAccess access, int n) {
        agents = new ArrayList<>();
        try {
            readConfig(input, access);
            addRandomAgents(access, n);
            writer = new FileWriter(new File(output), true);
        } catch (IOException ex) {
            Logger.getLogger(TestingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readConfig(String fileName, RecommenderAccess access) throws FileNotFoundException, IOException {
        BufferedReader bir = new BufferedReader(new FileReader(fileName));
        String line = bir.readLine();
        while (line != null) {
            String[] intrests = line.split(";");
            ProfileTestingAgent agent = new ProfileTestingAgent(access, agents.size(), intrests);
            agents.add(agent);
            line = bir.readLine();
        }
        bir.close();
    }

    public void start() throws IOException, InterruptedException {
        while (!stop) {
            double results = 0;
            double relevantResults = 0;
            double relevantResultsNotYetSeen = 0;
            double neededTime = 0;

            for (ProfileTestingAgent agent : agents) {
                TestResult r = agent.test();
                results += r.getResults();
                relevantResults += r.getRelevantResults();
                relevantResultsNotYetSeen += r.getRelevantResultsNotYetSeen();
                neededTime += r.getNeededTime();
            }
            
            results /= agents.size();
            relevantResults /= agents.size();
            relevantResultsNotYetSeen /= agents.size();
            neededTime /= agents.size();
            
            writer.write(results +";"+relevantResults+";"+relevantResultsNotYetSeen+";"+neededTime+"\n");
            writer.flush();
            Thread.sleep(60000);
        }
        writer.close();
    }

    public void stop() {
        stop = false;
    }

    private void addRandomAgents(RecommenderAccess access, int n) {
        for (int i = 0; i < n; i++){
            RandomProfileTestingAgent a = new RandomProfileTestingAgent(access, agents.size());
            agents.add(a);
        }
    }

}
