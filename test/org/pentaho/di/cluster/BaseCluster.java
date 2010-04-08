package org.pentaho.di.cluster;

import java.nio.charset.Charset;
import java.util.Arrays;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class BaseCluster extends TestCase {
	
	/*
	public void testIgnoreWhiteSpaces() {
		assertEqualsIgnoreWhitespaces("a  b   c", "a b c");
		assertEqualsIgnoreWhitespaces("a, b, c", "a,b,c   ");
		assertEqualsIgnoreWhitespacesAndCase("A  B   C", "a b c");
		assertEqualsIgnoreWhitespacesAndCase("a, b, c", "A,B,C  ");
	}
	*/
	
	public static TransExecutionConfiguration createClusteredTransExecutionConfiguration() {
		
		TransExecutionConfiguration config = new TransExecutionConfiguration();
		config.setExecutingClustered(true);
		config.setExecutingLocally(false);
		config.setExecutingRemotely(false);
		config.setClusterPosting(true);
		config.setClusterPreparing(true);
		config.setClusterStarting(true);
		config.setLogLevel(LogLevel.BASIC);
		
		// LogWriter.getInstance().setFilter(" server socket ");
		
		return config;
	}

	
	
	public TransMeta loadAndModifyTestTransformation(ClusterGenerator clusterGenerator, String filename) throws KettleException {
		TransMeta transMeta = new TransMeta(filename);
		
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
	
	
	
	
	protected void init() throws Exception {
    	// Bootstrap the Kettle API...
    	//
    	KettleEnvironment.init();
    	CentralLogStore.init(0, 60); // Keep all log rows for at least 60 minutes
	}

	protected String loadFileContent(VariableSpace space, String filename) throws Exception {
		String realFilename = space.environmentSubstitute(filename);
		return KettleVFS.getTextFileContent(realFilename, Charset.defaultCharset().name());
	}
	
	protected void assertEqualsIgnoreWhitespaces(String expected, String two) {
		String oneStripped = stripWhiteSpaces(expected);
		String twoStripped = stripWhiteSpaces(two);
		
		assertEquals(oneStripped, twoStripped);
	}
	
	protected void assertEqualsIgnoreWhitespacesAndCase(String expected, String actual) {
		assertEqualsIgnoreWhitespaces(expected.toUpperCase(), actual.toUpperCase());
	}

	private String stripWhiteSpaces(String one) {
		StringBuilder stripped = new StringBuilder();
		
		boolean previousWhiteSpace = false;
		
		for (char c : one.toCharArray()) {
			if (Character.isWhitespace(c)) {
				if (!previousWhiteSpace) {
					stripped.append(' '); // add a single white space, don't add a second
				}
				previousWhiteSpace=true;
			}
			else {
				if (c=='(' || c==')' || c=='|' || c=='-' || c=='+' || c=='/' || c=='*' || c=='{' || c=='}' || c==',' ) {
					int lastIndex = stripped.length()-1;
					if (stripped.charAt(lastIndex)==' ') {
						stripped.deleteCharAt(lastIndex);
					}
					previousWhiteSpace=true;
				} else {
					previousWhiteSpace=false;
				}
				stripped.append(c);
			}
		}
		
		// Trim the whitespace (max 1) at the front and back too...
		if (stripped.length() > 0 && Character.isWhitespace(stripped.charAt(0))) stripped.deleteCharAt(0);
		if (stripped.length() > 0 && Character.isWhitespace(stripped.charAt(stripped.length()-1))) stripped.deleteCharAt(stripped.length()-1);
		
		return stripped.toString();
	}

}
