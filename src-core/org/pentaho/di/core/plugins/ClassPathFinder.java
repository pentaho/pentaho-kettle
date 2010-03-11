package org.pentaho.di.core.plugins;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.logging.LogChannel;
import org.scannotation.AnnotationDB;

public class ClassPathFinder {

	private URLClassLoader urlClassLoader;
	private URL[] urls;
	
	public ClassPathFinder(URL[] urls) {
		this.urls = urls;
	}
	
	public <T> List<Class<? extends T>> findClassesImplementingInterface(Class<? extends T> interf, String packageLimitation) {
		List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
		
        long startTime = System.currentTimeMillis();

        // Scan all the URLs in the plugins, look for a class with the specified interface...
        //
		for (URL url : urls) {
			
			AnnotationDB db = new AnnotationDB();
			try {
				db.scanArchives(url);
				
				for (String key : db.getClassIndex().keySet()) {
					if (packageLimitation==null || key.startsWith(packageLimitation)) {
						try {
							ClassPool classPool = ClassPool.getDefault();
							String filename = URLDecoder.decode(url.getFile(), "UTF-8");
							classPool.appendClassPath(filename);
							
							CtClass ctClass = classPool.get(key);
				
							CtClass[] interfaces = ctClass.getInterfaces();
							for (CtClass check: interfaces) {
								if (check.getName().equals(interf.getName())) {
									try {
										List<URL> urlList = new ArrayList<URL>();
										urlList.add(url);
	
										// Also append all the files in the underlying lib folder if it exists...
										//
										String libFolderName = new File(filename).getParent()+"/lib";
										if (new File(libFolderName).exists()) {
											PluginFolder pluginFolder = new PluginFolder(libFolderName, false, true);
											FileObject[] libFiles = pluginFolder.findJarFiles();
											for (FileObject libFile : libFiles) {
												urlList.add(libFile.getURL());
											}
										}
										
										// Now create a new URLClassLoader for this one library.
										//
										KettleURLClassLoader urlClassLoader = new KettleURLClassLoader(urlList.toArray(new URL[urlList.size()]), getClass().getClassLoader());
										//for (URL u : urlClassLoader.getURLs()) System.out.println(u);
										Class<? extends T> clazz = urlClassLoader.loadClass(ctClass.getName()).asSubclass(interf);
										classes.add( clazz );
									} catch(Exception e) {
										LogChannel.GENERAL.logError("Unable to reach class "+ctClass.getName(), e);
									}
								}
							}
						} catch(NotFoundException e) {
							LogChannel.GENERAL.logDebug("File not found exception");
						} catch (UnsupportedEncodingException e) {
							LogChannel.GENERAL.logError("Unsupported encoding encountered", e);
						}
					}
				}
			} catch(Exception e) {
				LogChannel.GENERAL.logDetailed("Unable to scan for annotations in "+url, e);
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
}
