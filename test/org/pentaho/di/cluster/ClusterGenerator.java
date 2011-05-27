package org.pentaho.di.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.www.Carte;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.WebServer;

public class ClusterGenerator {

	public static final String TEST_CLUSTER_NAME = "test-cluster";
	
	/**
	 * We use one master and 3 slaves
	 */
	public static final SlaveServer[] LOCAL_TEST_SLAVES = new SlaveServer[] {
		new SlaveServer("test-localhost-8585-master", "127.0.0.1", "8585", "cluster", "cluster", null, null, null, true),
		new SlaveServer("test-localhost-8586-slave", "127.0.0.1", "8586", "cluster", "cluster", null, null, null, false),
		new SlaveServer("test-localhost-8587-slave", "127.0.0.1", "8587", "cluster", "cluster", null, null, null, false),
		new SlaveServer("test-localhost-8588-slave", "127.0.0.1", "8588", "cluster", "cluster", null, null, null, false),
	};
	
	private ClusterSchema clusterSchema;
	private List<Carte> carteList;
	
	public ClusterGenerator() throws KettleException {
		this.clusterSchema = new ClusterSchema();
		this.clusterSchema.setName(TEST_CLUSTER_NAME);
		this.clusterSchema.getSlaveServers().addAll(Arrays.asList(LOCAL_TEST_SLAVES));
		this.clusterSchema.setSocketsCompressed(false);
		this.clusterSchema.setBasePort("40000");
		this.clusterSchema.setSocketsBufferSize("2000");
		this.clusterSchema.setSocketsFlushInterval("5000");
		
		this.carteList = new ArrayList<Carte>();
	}
	
	private class CarteLauncher implements Runnable {
		private Carte carte;
		private String hostname;
		private int port;
		private Exception exception;
		private boolean failure;
		public CarteLauncher(String hostname, int port) {
			this.carte=null;
			this.hostname = hostname;
			this.port = port;
		}
		public void run() {
			try {
				SlaveServerConfig config = new SlaveServerConfig(hostname, port, false);
				carte = new Carte(config);
			} catch (Exception e) {
				this.exception = e;
				failure=true;
			}
		}
	}
	
	public void launchSlaveServers() throws Exception {
		
		// Launch the defined slave servers in a separate thread...
		//
		for (SlaveServer slaveServer : LOCAL_TEST_SLAVES) {
			final String hostname = slaveServer.getHostname();
			final int port = Const.toInt(slaveServer.getPort(), WebServer.PORT);
			CarteLauncher launcher = new CarteLauncher(hostname, port);
			Thread thread = new Thread(launcher);
			thread.start();
			// Wait until the carte object is available...
			while (launcher.carte==null && !launcher.failure) {
				Thread.sleep(100);
			}
			// Keep a list of launched servers
			if (launcher.carte!=null) {
				carteList.add(launcher.carte);
			}
			// If there is a failure, stop the servers already launched and throw the exception
			if (launcher.failure) {
				stopSlaveServers();
				throw launcher.exception; // throw the exception for good measure.
			}
		}
	}
	
	public void stopSlaveServers() throws Exception {
		for (Carte carte : carteList) {
			carte.getWebServer().stopServer();
		}
	}
	

	/**
	 * @return the clusterSchema
	 */
	public ClusterSchema getClusterSchema() {
		return clusterSchema;
	}

	/**
	 * @param clusterSchema the clusterSchema to set
	 */
	public void setClusterSchema(ClusterSchema clusterSchema) {
		this.clusterSchema = clusterSchema;
	}
}
