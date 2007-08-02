package org.pentaho.di.ui.spoon;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransSplitInfo;

public class SpoonTransSplitInfo implements TransSplitInfo
{
	private Spoon spoon;
	private boolean show;

	public SpoonTransSplitInfo(Spoon spoon, boolean show)
	{
		this.spoon = spoon;
		this.show = show;
	}

	public TransExecutionConfiguration getExecutionConfiguration()
	{
		return spoon.getExecutionConfiguration();
	}

	public void addTransMeta(TransMeta meta)
	{
		if (show)
			spoon.delegates.trans.addTransGraph(meta);
	}

	public void addMonitors(SlaveServer masterServer, SlaveServer[] slaves)
	{
		if (masterServer != null)
		{
			spoon.addSpoonSlave(masterServer);
			for (int i = 0; i < slaves.length; i++)
			{
				spoon.addSpoonSlave(slaves[i]);
			}
		}
	}

}
