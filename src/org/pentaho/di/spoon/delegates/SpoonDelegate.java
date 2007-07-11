package org.pentaho.di.spoon.delegates;

import org.pentaho.di.spoon.Spoon;

public abstract class SpoonDelegate
{
	protected Spoon spoon;
	
	protected SpoonDelegate(Spoon spoon)
	{
		this.spoon = spoon;
	}
}
