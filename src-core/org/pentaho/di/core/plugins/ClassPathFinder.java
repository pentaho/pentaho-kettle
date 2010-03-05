package org.pentaho.di.core.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.pentaho.di.core.logging.LogChannel;
import org.scannotation.AnnotationDB;

public class ClassPathFinder {

	private URLClassLoader urlClassLoader;
	private URL[] urls;
	private AnnotationDB	annotationDb;
	private Set<String>	classIndex;
	private ClassPool	classPool;
	
	public ClassPathFinder(URLClassLoader urlClassLoader) {
		this.urlClassLoader = urlClassLoader;
		urls = urlClassLoader.getURLs();
        annotationDb = PluginRegistry.getAnnotationDB();
        classIndex = annotationDb.getClassIndex().keySet();
		classPool = ClassPool.getDefault();
		for (URL url : urls) {
			try {
				classPool.appendClassPath( url.getFile() );
			} catch(NotFoundException e) {
				LogChannel.GENERAL.logError("File not found during append of class pool : "+url.getFile());
			}
		}
		
	}
	
	public <T> List<Class<? extends T>> findClassesImplementingInterface(Class<? extends T> interf, String packageLimitation) {
		List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
		
        long startTime = System.currentTimeMillis();
        
		for (String key : classIndex) {
			if (packageLimitation==null || key.startsWith(packageLimitation)) {
				try {
					CtClass ctClass = classPool.get(key);
		
					CtClass[] interfaces = ctClass.getInterfaces();
					for (CtClass check: interfaces) {
						if (check.getName().equals(interf.getName())) {
							try {
								Class<? extends T> clazz = Class.forName(ctClass.getName()).asSubclass(interf);
								classes.add( clazz );
							} catch(Exception e) {
								LogChannel.GENERAL.logDetailed("Unable to reach class "+ctClass.getName()+": "+e.getMessage());
							}
						}
					}
				} catch(NotFoundException e) {
					// Class not found in class path
				}
			}
		}
		LogChannel.GENERAL.logDetailed("Finished scan for "+interf.getCanonicalName()+" in "+(System.currentTimeMillis()-startTime)+"ms.");

		return classes;
	}
	
	public URLClassLoader getUrlClassLoader() {
		return urlClassLoader;
	}
	
	public URL[] getUrls() {
		return urls;
	}
	
	public ClassPool getClassPool() {
		return classPool;
	}
}
