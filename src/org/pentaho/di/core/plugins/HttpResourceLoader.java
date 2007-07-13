package org.pentaho.di.core.plugins;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ClassUtils;

public class HttpResourceLoader implements ResourceLoader {

	private ClassLoader classLoader;

	/**
	 * Create a new DefaultResourceLoader.
	 * <p>ClassLoader access will happen using the thread context class loader
	 * at the time of this ResourceLoader's initialization.
	 * @see java.lang.Thread#getContextClassLoader()
	 */
	public HttpResourceLoader() {
		this.classLoader = ClassUtils.getDefaultClassLoader();
	}

	public ClassLoader getClassLoader() {
		return this.classLoader;
	}


	public Resource getResource(String location) {	
		//try to parse it as a URL
		try
		{
			URL url = new URL(location);
			return new UrlResource(url);
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		
		return null;
		
	}

}
