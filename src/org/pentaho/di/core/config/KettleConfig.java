/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.config;

import java.beans.Introspector;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.SetNextRule;
import org.apache.commons.digester.SetPropertiesRule;
import org.pentaho.di.core.annotations.Inject;
import org.xml.sax.Attributes;

/**
 * The gateway for all configuration operations.
 * 
 * <h3>Configuration Managers Property Injection:</h3>
 * This class reads "<property>" elements from kettle-config.xml and attemps to
 * inject the value of such fields into the corresponding
 * <code>ConfigManager</code> implementation, following the rules established
 * the
 * 
 * @Inject annotation.
 * 
 * @see org.pentaho.core.annotations.Inject
 * 
 * @author Alex Silva
 * 
 */
public class KettleConfig
{
	private static final String KETTLE_CONFIG = "kettle-config/config";

	private static final String KETTLE_CONFIG_PROPERTY = KETTLE_CONFIG + "/property";

	private static final String KETTLE_CONFIG_CLASS = KETTLE_CONFIG + "/config-class";

	private static KettleConfig config;

	private Map<String, ConfigManager<?>> configs = new HashMap<String, ConfigManager<?>>();

	private KettleConfig()
	{
		Digester digester = createDigester();
		try
		{
			digester.parse(Thread.currentThread().getContextClassLoader().getResource(
					getClass().getPackage().getName().replace('.', '/') + "/kettle-config.xml"));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	

	public static KettleConfig getInstance()

	{
		if (config == null)
		{
			synchronized (KettleConfig.class)
			{
				config = new KettleConfig();
			}
		}

		return config;
	}

	public ConfigManager<?> getManager(String name)
	{
		return configs.get(name);
	}

	/**
	 * Returns all loaders defined in kettle-config.xml.
	 * 
	 * @return
	 */
	public Collection<ConfigManager<?>> getManagers()
	{
		return configs.values();
	}

	private Digester createDigester()
	{
		Digester digester = new Digester();
		digester.addObjectCreate(KETTLE_CONFIG, TempConfig.class);
		digester.addBeanPropertySetter(KETTLE_CONFIG_CLASS, "clazz");
		digester.addSetProperties(KETTLE_CONFIG, "id", "id");
		digester.addRule(KETTLE_CONFIG_PROPERTY, new SetPropertiesRule()
		{
			@Override
			public void begin(Attributes attrs) throws Exception
			{
				((TempConfig) digester.peek()).parms.put(attrs.getValue("name"), attrs.getValue("value"));
			}
		});

		digester.addRule(KETTLE_CONFIG, new SetNextRule("")
		{
			@SuppressWarnings("unchecked")
			public void end(String nameSpace, String name) throws Exception
			{
				TempConfig cfg = (TempConfig) digester.peek();

				// do the conversion here.
				Class<? extends ConfigManager> cfclass = Class.forName(cfg.clazz).asSubclass(
						ConfigManager.class);

				ConfigManager parms = cfclass.newInstance();
				// method injection
				inject(cfclass.getMethods(), cfg, parms);
				// field injection
				inject(cfclass.getDeclaredFields(), cfg, parms);
				KettleConfig.this.configs.put(cfg.id, parms);
			}
		});

		return digester;
	}

	private <E extends AccessibleObject> void inject(E[] elems, TempConfig cfg, ConfigManager<?> parms)
			throws IllegalAccessException, InvocationTargetException
	{
		for (AccessibleObject elem : elems)
		{
			Inject inj = elem.getAnnotation(Inject.class);
			if (inj != null)
			{
				// try to inject property from map.
				elem.setAccessible(true);
				String property = inj.property();
				// Can't think of any other way
				if (elem instanceof Method)
				{
					Method meth = (Method) elem;
					// find out what we are going to inject 1st
					property = property.equals("") ? Introspector.decapitalize(meth.getName().substring(3))
							: property;
					meth.invoke(parms, cfg.parms.get(property));

				} else if (elem instanceof Field)
				{
					Field field = (Field) elem;
					field.set(parms, cfg.parms.get(property.equals("") ? field.getName() : property));
				}
			}
		}
	}

	public static class TempConfig
	{
		private String clazz;

		private String id;

		private Map<String, String> parms = new HashMap<String, String>();

		public String getClazz()
		{
			return clazz;
		}

		public void setClazz(String clazz)
		{
			this.clazz = clazz;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

	}
}
