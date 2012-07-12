/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hadoop.hive.jdbc;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;

/**
 * A mixture of unit and integration tests for {@link HadoopConfigurationUtil}
 */
public class HadoopConfigurationUtilTest {

  @BeforeClass
  public static void init() throws KettleException {
    // Register a bogus HadoopCopyFilesPlugin. This is the plugin we key off of to find the Big Data Plugin
    KettleEnvironment.init();
    PluginMainClassType mainClassTypesAnnotation = KettleLifecyclePluginType.class.getAnnotation(PluginMainClassType.class);
    PluginInterface hadoopConfigurationBootstrap = new Plugin(new String[]{"HadoopConfigurationBootstrap"}, StepPluginType.class, mainClassTypesAnnotation.value(), "", "", "", null, false, false, new HashMap<Class<?>, String>(), new ArrayList<String>(), null, null);
    PluginRegistry.getInstance().registerPlugin(KettleLifecyclePluginType.class, hadoopConfigurationBootstrap);
  }

  @Test
  public void getBigDataPluginClassLoader() throws Exception {
    ClassLoader cl = HadoopConfigurationUtil.findBigDataPluginClassLoader();
    assertNotNull(cl);
  }

}
