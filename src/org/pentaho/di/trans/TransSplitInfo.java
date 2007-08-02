package org.pentaho.di.trans;

import org.pentaho.di.cluster.SlaveServer;

public interface TransSplitInfo
{
	
	
	/**
	 * Optional operation - used by GUI only I think (in this case spoon)
	 * @param meta
	 */
	public void addTransMeta(TransMeta meta);
	
	/**
	 * Optional operation 
	 * @param meta
	 */
	public void addMonitors(SlaveServer masterServer, SlaveServer[] slaves);

}
