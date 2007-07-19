package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.ui.spoon.Spoon;

public abstract class SpoonDelegate
{
	protected Spoon spoon;
	
	protected SpoonDelegate(Spoon spoon)
	{
		this.spoon = spoon;
	}
}
