package org.pentaho.di.core.lifecycle;

public interface LifeEventHandler
{
	void consume(LifeEventInfo info);
}
