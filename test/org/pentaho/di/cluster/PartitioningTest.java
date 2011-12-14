package org.pentaho.di.cluster;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

public class PartitioningTest extends BaseCluster {

	/**
	 * This test reads a CSV file in parallel on the cluster, one copy per slave.<br>
	 * It then partitions the data on id in 12 partitions (4 per slave) and keeps the data partitioned until written to file.<br>
	 * As such we expect 12 files on disk.<br>
	 * File: "partitioning-swimming-lanes-on-cluster.ktr"<br>
	 */
	public void testPartitioningSwimmingLanesOnCluster() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = loadAndModifyTestTransformation(clusterGenerator, "test/org/pentaho/di/cluster/partitioning-swimming-lanes-on-cluster.ktr");
			TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation(new LogChannel("cluster unit test <testParallelFileReadOnMaster>"), transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			
			String[] results = new String[] { "8", "9", "9", "9", "9", "8", "8", "8", "8", "8", "8", "8", };
			String[] files = new String[] { "000", "001", "002", "003", "004", "005", "006", "007", "008", "009", "010", "011", }; 
			for (int i=0;i<results.length;i++) {
				String filename = "${java.io.tmpdir}/partitioning-swimming-lanes-on-cluster-"+files[i]+".txt";
				String result = loadFileContent(transMeta, filename);
				assertEqualsIgnoreWhitespacesAndCase(results[i], result);
				
				// Remove the output file : we don't want to leave too much clutter around
				//
				FileObject file = KettleVFS.getFileObject(transMeta.environmentSubstitute(filename));
				file.delete();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}
	
	/**
	 * This test reads a CSV file in parallel on the cluster, one copy per slave.<br>
	 * It then partitions the data on id in 12 partitions (4 per slave).<br>
	 * After that it re-partitions the data in 9 partitions (3 per slave).<br>
	 * As such we expect 9 result files on disk.<br>
	 * File: "partitioning-repartitioning-on-cluster.ktr"<br>
	 */
	public void testPartitioningRepartitioningOnCluster() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = loadAndModifyTestTransformation(clusterGenerator, "test/org/pentaho/di/cluster/partitioning-repartitioning-on-cluster.ktr");
			TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation(new LogChannel("cluster unit test <testParallelFileReadOnMaster>"), transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			
			String[] results = new String[] { "8", "9", "9", "9", "9", "8", "8", "8", "8", "8", "8", "8", };
			String[] files = new String[] { "000", "001", "002", "003", "004", "005", "006", "007", "008", "009", "010", "011", }; 
			for (int i=0;i<results.length;i++) {
				String filename = "${java.io.tmpdir}/partitioning-repartitioning-on-cluster-"+files[i]+".txt";
				String result = loadFileContent(transMeta, filename);
				assertEqualsIgnoreWhitespacesAndCase(results[i], result);
				
				// Remove the output file : we don't want to leave too much clutter around
				//
				FileObject file = KettleVFS.getFileObject(transMeta.environmentSubstitute(filename));
				file.delete();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}
	
	 /**
   * Same as testPartitioningRepartitioningOnCluster() but passing the data to a non-partitioned step on the master.
   * 
   * File: "partitioning-repartitioning-on-cluster3.ktr"<br>
   */
  public void testPartitioningRepartitioningOnCluster3() throws Exception {
    init();
    
    ClusterGenerator clusterGenerator = new ClusterGenerator();
    try {
      clusterGenerator.launchSlaveServers();
      
      TransMeta transMeta = loadAndModifyTestTransformation(clusterGenerator, "test/org/pentaho/di/cluster/partitioning-repartitioning-on-cluster3.ktr");
      TransExecutionConfiguration config = createClusteredTransExecutionConfiguration();
      TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
      long nrErrors = Trans.monitorClusteredTransformation(new LogChannel("cluster unit test <testParallelFileReadOnMaster>"), transSplitter, null, 1);
      assertEquals(0L, nrErrors);
      
      String goldenData = "000;16\n001;17\n002;17\n003;17\n004;17\n005;16";
      String filename = "${java.io.tmpdir}/partitioning-repartitioning-on-cluster3.txt";
      String result = loadFileContent(transMeta, filename);
      assertEqualsIgnoreWhitespacesAndCase(goldenData, result);
        
      // Remove the output file : we don't want to leave too much clutter around
      //
      // FileObject file = KettleVFS.getFileObject(transMeta.environmentSubstitute(filename));
      // file.delete();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.toString());
    }
    finally {
      try {
        clusterGenerator.stopSlaveServers();
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.toString());
      }
    }
  }
}
