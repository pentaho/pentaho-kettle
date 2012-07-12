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

package org.pentaho.hadoop.shim;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;

public class MockHadoopConfigurationProvider implements HadoopConfigurationProvider {
  private List<? extends HadoopConfiguration> configs;
  private String activeId;
  
  public MockHadoopConfigurationProvider() {
    this(new ArrayList<HadoopConfiguration>(), null);
  }
  
  public MockHadoopConfigurationProvider(List<? extends HadoopConfiguration> configs, String activeId) {
    this.configs = configs;
    this.activeId = activeId;
  }
  
  @Override
  public boolean hasConfiguration(String id) {
    try {
      return getConfiguration(id) != null;
    } catch (ConfigurationException e) {
      return false;
    }
  }

  @Override
  public List<? extends HadoopConfiguration> getConfigurations() {
    return configs;
  }

  @Override
  public HadoopConfiguration getConfiguration(String id) throws ConfigurationException {
    for (HadoopConfiguration c : configs) {
      if (id.equals(c.getIdentifier())) {
        return c;
      }
    }
    return null;
  }

  @Override
  public HadoopConfiguration getActiveConfiguration() throws ConfigurationException {
    return getConfiguration(activeId);
  }

}
