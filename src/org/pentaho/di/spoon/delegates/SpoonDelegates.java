package org.pentaho.di.spoon.delegates;

import org.pentaho.di.spoon.Spoon;

public class SpoonDelegates
{
	public SpoonJobDelegate jobs;

	public SpoonTabsDelegate tabs;

	public SpoonTransformationDelegate trans;

	public SpoonSlaveDelegate slaves;

	public SpoonTreeDelegate tree;

	public SpoonStepsDelegate steps;

	public SpoonDBDelegate db;

	public SpoonDelegates(Spoon spoon)
	{
		jobs = new SpoonJobDelegate(spoon);
		tabs = new SpoonTabsDelegate(spoon);
		trans = new SpoonTransformationDelegate(spoon);
		tree = new SpoonTreeDelegate(spoon);
		slaves = new SpoonSlaveDelegate(spoon);
		steps = new SpoonStepsDelegate(spoon);
		db = new SpoonDBDelegate(spoon);
	}

}
