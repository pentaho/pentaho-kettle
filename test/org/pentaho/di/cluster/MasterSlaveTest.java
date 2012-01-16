/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.cluster;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

public class MasterSlaveTest extends BaseCluster {
  
  ClusterGenerator clusterGenerator;
  
  @Override
  protected void setUp() throws Exception {
    init();
    
    clusterGenerator = new ClusterGenerator();
    clusterGenerator.launchSlaveServers();
  }
  
  @Override
  protected void tearDown() throws Exception {
    clusterGenerator.stopSlaveServers();
  }
  
  public void testAll() throws Exception {
    runAllocatePorts();
    final int ITERATIONS=2;
    
    for (int i=0;i<ITERATIONS;i++) {
      runParallelFileReadOnMaster();
      runParallelFileReadOnMasterWithCopies();
      runParallelFileReadOnSlaves();
      runParallelFileReadOnSlavesWithPartitioning();
      runParallelFileReadOnSlavesWithPartitioning2();
      runMultipleCopiesOnMultipleSlaves();
      runMultipleCopiesOnMultipleSlaves2();
      runOneStepClustered();
    }
  }
	
  public void runAllocatePorts() throws Exception {
      ClusterSchema clusterSchema = clusterGenerator.getClusterSchema();
      SlaveServer master = clusterSchema.findMaster();
      List<SlaveServer> slaves = clusterSchema.getSlaveServersFromMasterOrLocal();
      String clusteredRunId = UUID.randomUUID().toString();
      
      SlaveServer slave1 = slaves.get(0);
      SlaveServer slave2 = slaves.get(1);
      SlaveServer slave3 = slaves.get(2);
      
      int port1 = master.allocateServerSocket(clusteredRunId, 40000, "localhost", "trans1", 
          master.getName(), "A", "0", 
          slave1.getName(), "B", "0");
      assertEquals(40000, port1);
      
      int port1b = master.allocateServerSocket(clusteredRunId, 40000, "localhost", "trans1", 
          master.getName(), "A", "0", 
          slave1.getName(), "B", "0");
      assertEquals(port1, port1b);
      
      int port2 = master.allocateServerSocket(clusteredRunId, 40000, "localhost", "trans1", 
          master.getName(), "A", "0", 
          slave2.getName(), "B", "0");
      assertEquals(40001, port2);

      int port3 = master.allocateServerSocket(clusteredRunId, 40000, "localhost", "trans1", 
          master.getName(), "A", "0", 
          slave3.getName(), "B", "0");
      assertEquals(40002, port3);
      
      master.deAllocateServerSockets("trans1", clusteredRunId);
      
      port1 = master.allocateServerSocket(clusteredRunId, 40000, "localhost", "trans2", 
          master.getName(), "A", "0", 
          slave1.getName(), "B", "0");
      assertEquals(40000, port1);
      
      master.deAllocateServerSockets("trans2", clusteredRunId);
  }	

	/**
	 * This test reads a CSV file in parallel on the master in 1 copy.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 * We want to make sure that only 1 copy is considered.<br>
	 */
	public void runParallelFileReadOnMaster() throws Exception {
		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
		    "test/org/pentaho/di/cluster/test-parallel-file-read-on-master.ktr");
		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
		LogChannel logChannel = createLogChannel("cluster unit test <testParallelFileReadOnMaster>");
		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
		assertEquals(0L, nrErrors);
		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-master-result.txt");
		assertEqualsIgnoreWhitespacesAndCase("100", result);
	}

	private static LogChannel createLogChannel(String string) {
	  LogChannel logChannel = new LogChannel(string);
	  logChannel.setLogLevel(LogLevel.BASIC);
	  return logChannel;
  }

  /**
	 * This test reads a CSV file in parallel on the master in 3 copies.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void runParallelFileReadOnMasterWithCopies() throws Exception {
		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
		    "test/org/pentaho/di/cluster/test-parallel-file-read-on-master-with-copies.ktr");
		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
		LogChannel logChannel = createLogChannel("cluster unit test <runParallelFileReadOnMasterWithCopies>");
		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
		assertEquals(0L, nrErrors);
		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-master-result-with-copies.txt");
		assertEqualsIgnoreWhitespacesAndCase("100", result);
	}
	

	/**
	 * This test reads a CSV file in parallel on all 3 slaves, each with 1 copy.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void runParallelFileReadOnSlaves() throws Exception {
		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
		    "test/org/pentaho/di/cluster/test-parallel-file-read-on-slaves.ktr");
		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
		LogChannel logChannel = createLogChannel("cluster unit test <runParallelFileReadOnSlaves>");
		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
		assertEquals(0L, nrErrors);
		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-slaves.txt");
		assertEqualsIgnoreWhitespacesAndCase("100", result);
	}
	
	/**
	 * This test reads a CSV file in parallel on all 3 slaves, each with 4 partitions.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void runParallelFileReadOnSlavesWithPartitioning() throws Exception {
		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
		    "test/org/pentaho/di/cluster/test-parallel-file-read-on-slaves-with-partitioning.ktr");
		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
		LogChannel logChannel = createLogChannel("cluster unit test <runParallelFileReadOnSlavesWithPartitioning>");
		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
		assertEquals(0L, nrErrors);
		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-slaves-with-partitioning.txt");
		assertEqualsIgnoreWhitespacesAndCase("100", result);
	}
	
	/**
	 * This test reads a CSV file in parallel on all 3 slaves, each with 4 partitions.<br>
	 * This is a variation on the test right above, with 2 steps in sequence in clustering & partitioning.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void runParallelFileReadOnSlavesWithPartitioning2() throws Exception {
		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
		    "test/org/pentaho/di/cluster/test-parallel-file-read-on-slaves-with-partitioning2.ktr");
		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
		LogChannel logChannel = createLogChannel("cluster unit test <runParallelFileReadOnSlavesWithPartitioning2>");
		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
		assertEquals(0L, nrErrors);
		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-slaves-with-partitioning2.txt");
		assertEqualsIgnoreWhitespacesAndCase("100", result);
	}
	
	/**
     * This test reads a CSV file and sends the data to 3 copies on 3 slave servers.<br>
     */
    public void runMultipleCopiesOnMultipleSlaves2() throws Exception {
  		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
  		    "test/org/pentaho/di/cluster/test-hops-between-multiple-copies-steps-on-cluster.ktr");
  		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
  		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
  		LogChannel logChannel = createLogChannel("cluster unit test <runMultipleCopiesOnMultipleSlaves2>");
  		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
  		assertEquals(0L, nrErrors);
  		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-multiple-copies-on-multiple-slaves2.txt");
  		assertEqualsIgnoreWhitespacesAndCase("90000", result);
    }


    /**
	 * This test reads a CSV file and sends the data to 3 copies on 3 slave servers.<br>
	 */
	public void runMultipleCopiesOnMultipleSlaves() throws Exception {
		TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
		    "test/org/pentaho/di/cluster/test-multiple-copies-on-multiple-slaves.ktr");
		TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
		TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
		LogChannel logChannel = createLogChannel("cluster unit test <testMultipleCopiesOnMultipleSlaves>");
		long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
		assertEquals(0L, nrErrors);
		String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-multiple-copies-on-multiple-slaves.txt");
		assertEqualsIgnoreWhitespacesAndCase("100", result);
	}

  /**
   * This test generates rows on the master, generates random values clustered and brings them back the master.<br>
   * See also: PDI-6324 : Generate Rows to a clustered step ceases to work
   */
  public void runOneStepClustered() throws Exception {
    TransMeta transMeta = loadTransMetaReplaceSlavesInCluster(clusterGenerator, 
        "test/org/pentaho/di/cluster/one-step-clustered.ktr");
    TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
    TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
    LogChannel logChannel = createLogChannel("cluster unit test <runOneStepClustered>");
    long nrErrors = Trans.monitorClusteredTransformation(logChannel, transSplitter, null, 1);
    assertEquals(0L, nrErrors);
    String result = loadFileContent(transMeta, "${java.io.tmpdir}/one-step-clustered.txt");
    assertEqualsIgnoreWhitespacesAndCase("10000", result);
  }
	
	
	
	
	
	
	
	
	
	
	
	
	
	

  private static TransMeta loadTransMetaReplaceSlavesInCluster(ClusterGenerator clusterGenerator, String testFilename) throws KettleException {
    TransMeta transMeta = new TransMeta(testFilename);
    
    // Add the slave servers
    //
    for (SlaveServer slaveServer : ClusterGenerator.LOCAL_TEST_SLAVES) {
      transMeta.getSlaveServers().add(slaveServer);
    }
    
    // Replace the slave servers in the specified cluster schema...
    //
    ClusterSchema clusterSchema = transMeta.findClusterSchema(ClusterGenerator.TEST_CLUSTER_NAME);
    assertNotNull("Cluster schema '"+ClusterGenerator.TEST_CLUSTER_NAME+"' couldn't be found", clusterSchema);
    clusterSchema.getSlaveServers().clear();
    clusterSchema.getSlaveServers().addAll(Arrays.asList(ClusterGenerator.LOCAL_TEST_SLAVES));
    
    return transMeta;
  }
    
}
