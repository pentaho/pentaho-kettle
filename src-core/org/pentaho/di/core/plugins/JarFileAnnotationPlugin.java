package org.pentaho.di.core.plugins;

import java.net.URL;

import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

public class JarFileAnnotationPlugin {
	private URL			jarFile;
	private URL pluginFolder;
	private String className;
	/**
	 * @param jarFile
	 * @param classFile
	 * @param annotation
	 */
	public JarFileAnnotationPlugin(String className, URL jarFile, URL pluginFolder) {
	  this.className = className;
		this.jarFile = jarFile;
		this.pluginFolder = pluginFolder;
	}

	@Override
	public String toString() {
		return jarFile.toString();
	}
	
	/**
	 * @return the jarFile
	 */
	public URL getJarFile() {
		return jarFile;
	}

	/**
	 * @param jarFile
	 *            the jarFile to set
	 */
	public void setJarFile(URL jarFile) {
		this.jarFile = jarFile;
	}

  public URL getPluginFolder() {
    return pluginFolder;
  }

  public void setPluginFolder(URL pluginFolder) {
    this.pluginFolder = pluginFolder;
  }
	
  public String getClassName(){
    return className;
  }

}
