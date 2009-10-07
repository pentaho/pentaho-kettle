package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.ResolverUtil;

public class LifecycleSupport implements LifecycleListener
{
	private Set<LifecycleListener> lifeListeners;

	private static LogChannelInterface log = new LogChannel("Spoon");

	public LifecycleSupport()
	{
		ResolverUtil<LifecycleListener> listeners = new ResolverUtil<LifecycleListener>();
		listeners.find(new ResolverUtil.IsA(LifecycleListener.class), "org.pentaho.di.core.lifecycle.pdi");
		Set<Class<? extends LifecycleListener>> listenerClasses = listeners.getClasses();

		lifeListeners = new HashSet<LifecycleListener>(listenerClasses.size());

		for (Class<? extends LifecycleListener> clazz : listenerClasses)
		{
			try
			{
				lifeListeners.add(clazz.newInstance());
			} 
			catch (Throwable e)
			{
				log.logError("Unable to init listener:" + e.getMessage(), new Object[] {});
				continue;
			}
		}
	}

	public void onStart(LifeEventHandler handler) throws LifecycleException
	{
		for (LifecycleListener listener : lifeListeners)
			listener.onStart(handler);
	}

	public void onExit(LifeEventHandler handler) throws LifecycleException
	{
		for (LifecycleListener listener : lifeListeners)
			listener.onExit(handler);
	}
}
