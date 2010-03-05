package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.scannotation.AnnotationDB;

public class LifecycleSupport implements LifecycleListener
{
	private Set<LifecycleListener> lifeListeners;

	public LifecycleSupport()
	{
		// ResolverUtil<LifecycleListener> listeners = new ResolverUtil<LifecycleListener>();
		// listeners.find(new ResolverUtil.IsA(LifecycleListener.class), "org.pentaho.di.core.lifecycle.pdi");
		// Set<Class<? extends LifecycleListener>> listenerClasses = listeners.getClasses();

		lifeListeners = new HashSet<LifecycleListener>();

        long startTime = System.currentTimeMillis();
        AnnotationDB db = PluginRegistry.getAnnotationDB();
        Set<String> classIndex = db.getClassIndex().keySet();
		ClassPool classPool = ClassPool.getDefault();
		for (String key : classIndex) {
			if (key.startsWith("org.pentaho.di.core.lifecycle.pdi")) {
				try {
					CtClass ctClass = classPool.get(key);
		
					CtClass[] interfaces = ctClass.getInterfaces();
					for (CtClass interf : interfaces) {
						if (interf.getName().equals(LifecycleListener.class.getName())) {
							try {
								Class<LifecycleListener> clazz = (Class<LifecycleListener>) Class.forName(ctClass.getName());
								lifeListeners.add( clazz.newInstance() );
							} catch(Exception e) {
								LogChannel.GENERAL.logDetailed("Unable to reach class "+ctClass.getName()+": "+e.getMessage());
							}
						}
					}
				} catch(NotFoundException e) {
					// System.out.println("        - interfaces not found for class: "+ctClass.getName());
				}
			}
		}
		LogChannel.GENERAL.logBasic("Finished lifecycle listener scan in "+(System.currentTimeMillis()-startTime)+"ms.");

		/*
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
		}*/
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
