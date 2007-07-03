package org.pentaho.di.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class was copied from Stripes -
 * http://stripes.mc4j.org/confluence/display/stripes/Home
 * 
 * @param <T>
 */
public class ResolverUtil<T>
{

	private static final Log log = LogFactory.getLog(ResolverUtil.class);

	public static interface Test
	{

		boolean matches(Class<?> type);
	}

	public static class IsA implements Test
	{

		private Class<?> parent;

		public IsA(Class<?> parentType)
		{
			super();
			this.parent = parentType;
		}

		public boolean matches(Class<?> type)
		{
			return type != null && parent.isAssignableFrom(type);
		}

		@Override()
		public String toString()
		{
			return "is assignable to " + parent.getSimpleName();
		}
	}

	public static class NameEndsWith implements Test
	{

		private String suffix;

		public NameEndsWith(String suffix)
		{
			super();
			this.suffix = suffix;
		}

		public boolean matches(Class<?> type)
		{
			return type != null && type.getName().endsWith(suffix);
		}

		@Override()
		public String toString()
		{
			return "ends with the suffix " + suffix;
		}
	}

	public static class AnnotatedWith implements Test
	{

		private Class<? extends Annotation> annotation;

		public AnnotatedWith(Class<? extends Annotation> annotation)
		{
			super();
			this.annotation = annotation;
		}

		public boolean matches(Class<?> type)
		{
			return type != null && type.isAnnotationPresent(annotation);
		}

		@Override()
		public String toString()
		{
			return "annotated with @" + annotation.getSimpleName();
		}
	}

	private Set<Class<? extends T>> matches = new HashSet<Class<? extends T>>();

	private ClassLoader classloader;

	public int size()
	{
		return matches.size();
	}

	public Set<Class<? extends T>> getClasses()
	{
		return matches;
	}

	public ClassLoader getClassLoader()
	{
		return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
	}

	public void setClassLoader(ClassLoader classloader)
	{
		this.classloader = classloader;
	}

	public void findImplementations(Class parent, String... packageNames)
	{
		if (packageNames == null)
			return;
		Test test = new IsA(parent);
		for (String pkg : packageNames)
		{
			findInPackage(test, pkg);
		}
	}

	public void findSuffix(String suffix, String... packageNames)
	{
		if (packageNames == null)
			return;
		Test test = new NameEndsWith(suffix);
		for (String pkg : packageNames)
		{
			findInPackage(test, pkg);
		}
	}

	public void findAnnotated(Class<? extends Annotation> annotation, String... packageNames)
	{
		if (packageNames == null)
			return;
		Test test = new AnnotatedWith(annotation);
		for (String pkg : packageNames)
		{
			findInPackage(test, pkg);
		}
	}

	public void find(Test test, String... packageNames)
	{
		if (packageNames == null)
			return;
		for (String pkg : packageNames)
		{
			findInPackage(test, pkg);
		}
	}

	public void findInPackage(Test test, String packageName)
	{
		packageName = packageName.replace('.', '/');
		ClassLoader loader = getClassLoader();
		Enumeration<URL> urls;
		try
		{
			urls = loader.getResources(packageName);
		} catch (IOException ioe)
		{
			log.warn("Could not read package: " + packageName, ioe);
			return;
		}
		while (urls.hasMoreElements())
		{
			try
			{
				String urlPath = urls.nextElement().getFile();
				urlPath = URLDecoder.decode(urlPath, "UTF-8");
				if (urlPath.startsWith("file:"))
				{
					urlPath = urlPath.substring(5);
				}
				if (urlPath.indexOf('!') > 0)
				{
					urlPath = urlPath.substring(0, urlPath.indexOf('!'));
				}
				log.info("Scanning for classes in [" + urlPath + "] matching criteria: " + test);
				File file = new File(urlPath);
				if (file.isDirectory())
				{
					loadImplementationsInDirectory(test, packageName, file);
				} else
				{
					loadImplementationsInJar(test, packageName, file);
				}
			} catch (IOException ioe)
			{
				log.warn("could not read entries", ioe);
			}
		}
	}

	private void loadImplementationsInDirectory(Test test, String parent, File location)
	{
		File[] files = location.listFiles();
		StringBuilder builder = null;
		for (File file : files)
		{
			builder = new StringBuilder(100);
			builder.append(parent).append("/").append(file.getName());
			String packageOrClass = (parent == null ? file.getName() : builder.toString());
			if (file.isDirectory())
			{
				loadImplementationsInDirectory(test, packageOrClass, file);
			} else if (file.getName().endsWith(".class"))
			{
				addIfMatching(test, packageOrClass);
			}
		}
	}

	private void loadImplementationsInJar(Test test, String parent, File jarfile)
	{
		try
		{
			JarEntry entry;
			JarInputStream jarStream = new JarInputStream(new FileInputStream(jarfile));
			while ((entry = jarStream.getNextJarEntry()) != null)
			{
				String name = entry.getName();
				if (!entry.isDirectory() && name.startsWith(parent) && name.endsWith(".class"))
				{
					addIfMatching(test, name);
				}
			}
		} catch (IOException ioe)
		{
			log.error("Could not search jar file \\\'" + jarfile + "\\\' for classes matching criteria: "
					+ test + " due to an IOException", ioe);
		}
	}

	@SuppressWarnings("unchecked")
	protected void addIfMatching(Test test, String fqn)
	{
		try
		{
			String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
			ClassLoader loader = getClassLoader();
			if (log.isDebugEnabled())
			{
				log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");
			}
			Class<?> type = loader.loadClass(externalName);

			if (test.matches(type))
			{
				matches.add((Class<T>) type);
			}
		} catch (Throwable t)
		{
			log.warn("Could not examine class \\\'" + fqn + "\\\' due to a " + t.getClass().getName()
					+ " with message: " + t.getMessage());
		}
	}
}
