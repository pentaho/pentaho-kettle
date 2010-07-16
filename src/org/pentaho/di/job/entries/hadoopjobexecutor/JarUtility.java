package org.pentaho.di.job.entries.hadoopjobexecutor;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarUtility {

  public static List<Class<?>> getClassesInJarWithMain(String jarUrl, ClassLoader parentClassloader) throws MalformedURLException {
    ArrayList<Class<?>> mainClasses = new ArrayList<Class<?>>();
    List<Class<?>> allClasses = JarUtility.getClassesInJar(jarUrl, parentClassloader);
    for (Class<?> clazz : allClasses) {
      try {
        Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
        if (Modifier.isStatic(mainMethod.getModifiers())) {
          mainClasses.add(clazz);
        }
      } catch (Throwable ignored) {
      }
    }
    return mainClasses;
  }

  public static List<Class<?>> getClassesInJar(String jarUrl, ClassLoader parentClassloader) throws MalformedURLException {

    URL url = new URL(jarUrl);
    URL[] urls = new URL[] { url };
    URLClassLoader loader = new URLClassLoader(urls, parentClassloader);

    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

    try {
      JarInputStream jarFile = new JarInputStream(new FileInputStream(new File(url.toURI())));
      JarEntry jarEntry;

      while (true) {
        jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        if (jarEntry.getName().endsWith(".class")) {
          String className = jarEntry.getName().substring(0, jarEntry.getName().indexOf(".class")).replaceAll("/", "\\.");
          classes.add(loader.loadClass(className));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return classes;
  }

}
