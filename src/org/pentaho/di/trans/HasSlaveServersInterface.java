package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;

public interface HasSlaveServersInterface {
	public List<SlaveServer> getSlaveServers();
}
