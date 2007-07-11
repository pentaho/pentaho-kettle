package org.pentaho.ui;

import org.pentaho.di.spoon.Spoon;

public abstract class SpoonDelegate
{
	protected Spoon spoon;
	
	protected SpoonDelegate(Spoon spoon)
	{
		this.spoon = spoon;
	}
}
