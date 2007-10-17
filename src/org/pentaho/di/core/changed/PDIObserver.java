package org.pentaho.di.core.changed;

import org.pentaho.di.core.changed.ChangedFlagInterface;

public interface PDIObserver 
{
	public void update(ChangedFlagInterface o, Object arg);
	
}
