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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.SetNextRule;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.pentaho.di.core.annotations.Inject;
import org.pentaho.di.core.exception.KettleConfigException;

/**
 * A custom <code>ConfigManager</code> implementation to load configuration
 * parameters from XML files using commons-digester.
 * 
 * <p>
 * Injection:
 * </p>
 * Three fields are injected: rulesURL, configURL and setNext. The values from
 * these fields are read from kettle-config.xml, as follows: <config
 * id="steps-config">
 * <config-class>org.pentaho.di.core.config.DigesterConfigParameters</config-class>
 * <property name="configURL" value="kettle-steps.xml"/> <property
 * name="rulesURL" value="org/pentaho/di/core/config/steps-rules.xml"/>
 * <property name="setNext" value="steps/step"/> </config>
 * 
 * @author Alex Silva
 * 
 * @param <T>
 */
public class DigesterConfigManager<T> extends BasicConfigManager<T>
{
	@Inject
	private String rulesURL;

	@Inject
	private String configURL;

	@Inject
	private String setNext;

	/**
	 * Loads the configuration parameters by delegating to commons digester.
	 */
	public Collection<T> load() throws KettleConfigException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		Digester digester = DigesterLoader.createDigester(loader.getResource(rulesURL));

		final Set<T> configObjs = new LinkedHashSet<T>();

		digester.addRule(setNext, new SetNextRule("")
		{
			@SuppressWarnings("unchecked")
			public void end(String nameSpace, String name) throws Exception
			{
				configObjs.add((T) digester.peek());
			}
		});

		try
		{
			digester.parse(loader.getResource(configURL));
		} catch (Exception e)
		{
			throw new KettleConfigException(e);
		}

		return configObjs;
	}

}
