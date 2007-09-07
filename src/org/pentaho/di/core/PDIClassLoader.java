package org.pentaho.di.core;

import java.net.URL;
import java.net.URLClassLoader;

public class PDIClassLoader extends URLClassLoader
{
	
	public PDIClassLoader(URL[] url,ClassLoader parent)
	{
		super (url,parent);
	}
	
	public PDIClassLoader(ClassLoader parent)
	{
		super(new URL[]{},parent);
	}
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		try
		{
			return super.loadClass(name, resolve);
		} catch (NoClassDefFoundError e)
		{
			return super.findClass(name);
		}
	}

}
