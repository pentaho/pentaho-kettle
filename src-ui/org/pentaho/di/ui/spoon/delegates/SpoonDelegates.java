package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.ui.spoon.delegates.SpoonDBDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonJobDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonSlaveDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonStepsDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonTabsDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonTransformationDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegate;
import org.pentaho.di.ui.spoon.Spoon;

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
