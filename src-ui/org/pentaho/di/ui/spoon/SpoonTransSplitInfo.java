package org.pentaho.di.ui.spoon;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.trans.BaseTransSplitInfo;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class SpoonTransSplitInfo extends BaseTransSplitInfo
{
	private Spoon spoon;

	public SpoonTransSplitInfo(Spoon spoon, TransMeta transMeta,boolean show,boolean post, boolean prepare, boolean start)
	{
		super(transMeta);
		this.spoon = spoon;
		this.show = show;
		this.post = post;
		this.prepare = prepare;
		this.start = start;
	}

	public TransExecutionConfiguration getExecutionConfiguration()
	{
		return spoon.getExecutionConfiguration();
	}

	public void addTransGraph(TransMeta meta)
	{
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
