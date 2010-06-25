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

  public static List<Class<?>> getClassesInJarWithMain(String jarUrl) throws MalformedURLException {
    ArrayList<Class<?>> mainClasses = new ArrayList<Class<?>>();
    List<Class<?>> allClasses = JarUtility.getClassesInJar(jarUrl);
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

  public static List<Class<?>> getClassesInJar(String jarUrl) throws MalformedURLException {

    URL url = new URL(jarUrl);
    URL[] urls = new URL[] { url };
    URLClassLoader loader = new URLClassLoader(urls);

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

  /**
   * @throws MalformedURLException
   * 
   */
  public static void main(String[] args) throws MalformedURLException {
    List<Class<?>> list = JarUtility.getClassesInJar("file:///C:/development-pentaho/pentaho-hdfs-vfs/test-lib/commons-io-1.4.jar");
    for (Class<?> clazz : list) {
      System.out.println("Found: " + clazz.getName());
    }
  }
}
