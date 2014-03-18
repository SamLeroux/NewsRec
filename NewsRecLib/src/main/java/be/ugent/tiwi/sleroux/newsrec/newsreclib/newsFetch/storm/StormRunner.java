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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.newsFetch.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;

/**
 * Utility class to start a local cluster to run a Storm topology.
 * @author Sam Leroux
 */
public final class StormRunner {
    private static LocalCluster cluster;

    private StormRunner() {
    }

    /**
     * Creates a new cluster if there is no running cluster, otherwise deploys 
     * the topology to the running cluster.
     * @param topology The topology to run
     * @param topologyName The name of the new topology
     * @param conf The configuration for the new topology.
     * @throws InterruptedException
     * @see Config
     */
    public static void runTopologyLocally(StormTopology topology, String topologyName, Config conf)
            throws InterruptedException {
        if (cluster == null){
        cluster = new LocalCluster();
        }
        cluster.submitTopology(topologyName, conf, topology);
    }

    /**
     * Stops a running cluster.
     * @param name the name of the cluster to stop.
     */
    public static void stop(String name) {
        cluster.killTopology(name);
    }
    
    public static void shutdown(){
        cluster.shutdown();
        cluster = null;
    }
}
