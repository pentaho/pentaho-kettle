package org.pentaho.di.trans;

import org.pentaho.di.cluster.SlaveServer;

public interface TransSplitInfo
{
	public boolean start();
	
	public TransMeta getTransMeta();
	
	public TransExecutionConfiguration getExecutionConfiguration();
		
	public boolean show();
	
	public boolean post();
	
	public boolean prepare();
	
	/**
	 * Optional operation - used by GUI only I think (in this case spoon)
	 * @param meta
	 */
	public void addTransGraph(TransMeta meta);
	
	/**
	 * Optional operation 
	 * @param meta
	 */
	public void addMonitors(SlaveServer masterServer, SlaveServer[] slaves);

}
