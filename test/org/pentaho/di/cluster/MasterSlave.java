package org.pentaho.di.cluster;

import java.util.Arrays;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

public class MasterSlave extends BaseCluster {

	public void testStartStopSlaveServers() {
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			clusterGenerator.stopSlaveServers();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	

	/**
	 * This test reads a CSV file in parallel on the master in 3 copies.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void testParallelFileReadOnMaster() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = generateParallelFileReadOnMasterTransMeta(clusterGenerator);
			TransExecutionConfiguration config = new TransExecutionConfiguration();
			config.setExecutingClustered(true);
			config.setExecutingLocally(false);
			config.setExecutingRemotely(false);
			config.setClusterPosting(true);
			config.setClusterPreparing(true);
			config.setClusterStarting(true);
			config.setLogLevel(LogWriter.LOG_LEVEL_BASIC);
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation("testParallelFileReadOnMaster", transSplitter, null);
			assertEquals(0L, nrErrors);
			String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-master-result.txt");
			assertEqualsIgnoreWhitespacesAndCase("100", result);
			
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


	private TransMeta generateParallelFileReadOnMasterTransMeta(ClusterGenerator clusterGenerator) throws KettleXMLException {
		TransMeta transMeta = new TransMeta("test/org/pentaho/di/cluster/test-parallel-file-read-on-master.ktr");
		
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
