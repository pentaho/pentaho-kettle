package org.pentaho.di.core.plugins;

import java.net.URL;

import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

public class JarFileAnnotationPlugin {
	private URL			jarFile;
	private ClassFile	classFile;
	private Annotation	annotation;

	/**
	 * @param jarFile
	 * @param classFile
	 * @param annotation
	 */
	public JarFileAnnotationPlugin(URL jarFile, ClassFile classFile, Annotation annotation) {
		this.jarFile = jarFile;
		this.classFile = classFile;
		this.annotation = annotation;
	}

	@Override
	public String toString() {
		return jarFile.toString()+" - "+classFile.getName()+" - "+annotation.getTypeName();
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

	/**
	 * @return the classFile
	 */
	public ClassFile getClassFile() {
		return classFile;
	}

	/**
	 * @param classFile
	 *            the classFile to set
	 */
	public void setClassFile(ClassFile classFile) {
		this.classFile = classFile;
	}

	/**
	 * @return the annotation
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * @param annotation
	 *            the annotation to set
	 */
	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

}
