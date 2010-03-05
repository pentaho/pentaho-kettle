package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.ClassPathFinder;
import org.pentaho.di.core.plugins.PluginRegistry;

public class LifecycleSupport implements LifecycleListener
{
	private Set<LifecycleListener> lifeListeners;

	public LifecycleSupport()
	{
		lifeListeners = new HashSet<LifecycleListener>();
        ClassPathFinder classPathFinder = PluginRegistry.getClassPathFinder();
        List<Class<? extends LifecycleListener>> classList = classPathFinder.findClassesImplementingInterface(LifecycleListener.class, "org.pentaho.di.core.lifecycle.pdi");
        for (Class<? extends LifecycleListener> clazz : classList) {
        	try {
        		lifeListeners.add( clazz.newInstance() );
        	} catch(IllegalAccessException e) {
        		LogChannel.GENERAL.logError("Illegal access to class "+clazz+", ignored.");
        	} catch (InstantiationException e) {
        		LogChannel.GENERAL.logError("Could not instatiate class class "+clazz+", ignored.");
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
