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

package org.pentaho.hadoop.mapreduce.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.hadoop.mapreduce.MRUtil;


/**
 * Tests for {@link MRUtil}.
 */
public class MRUtilTest {

  @Test
  public void getPluginDirProperty() throws KettleException {
    final String USER_DIR = System.getProperty("user.dir");
    
    final Configuration c = new Configuration();
    assertNull(c.get(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR));

    String pluginDirProperty = MRUtil.getPluginDirProperty(c);
    assertTrue("Plugin Directory Property not configured as expected: " + pluginDirProperty, pluginDirProperty.endsWith(USER_DIR));
  }
  
  @Test
  public void getPluginDirProperty_explicitly_set() throws KettleException {
    final String PLUGIN_DIR = "/opt/pentaho";
    final Configuration c = new Configuration();
    // Working directory will be used for the plugin directory if it is not explicitly provided
    c.set(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR, PLUGIN_DIR);
    String pluginDirProperty = MRUtil.getPluginDirProperty(c);
    assertTrue("Plugin Directory Property not configured as expected: " + pluginDirProperty, pluginDirProperty.endsWith(PLUGIN_DIR));
  }

  @Test
  public void getKettleHomeProperty() {
    final String USER_DIR = System.getProperty("user.dir");
    final Configuration c = new Configuration();
    String kettleHome = MRUtil.getKettleHomeProperty(c);
    assertEquals(USER_DIR, kettleHome);
  }

  @Test
  public void getKettleHomeProperty_explicitly_set() {
    final String KETTLE_HOME = "/my/kettle";
    final Configuration c = new Configuration();
    // Working directory will be used for Kettle Home if it is not explicitly provided
    c.set(MRUtil.PROPERTY_PENTAHO_KETTLE_HOME, KETTLE_HOME);
    String kettleHome = MRUtil.getKettleHomeProperty(c);
    assertEquals(KETTLE_HOME, kettleHome);
  }
}
