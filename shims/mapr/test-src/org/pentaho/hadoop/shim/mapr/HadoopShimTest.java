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

package org.pentaho.hadoop.shim.mapr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.junit.Test;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.HadoopConfigurationFileSystemManager;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;

public class HadoopShimTest {

  @Test
  public void onLoad() throws Exception {
    HadoopConfigurationProvider configProvider = new HadoopConfigurationProvider() {
      @Override
      public boolean hasConfiguration(String id) {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public List<? extends HadoopConfiguration> getConfigurations() {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public HadoopConfiguration getConfiguration(String id) throws ConfigurationException {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public HadoopConfiguration getActiveConfiguration() throws ConfigurationException {
        throw new UnsupportedOperationException();
      }
    };
    DefaultFileSystemManager delegate = new DefaultFileSystemManager();
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, delegate);
    assertFalse(fsm.hasProvider("hdfs"));

    HadoopShim shim = new HadoopShim();
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", shim, null, null, null);

    shim.onLoad(config, fsm);
    
    assertNotNull(shim.getDistributedCacheUtil());
  }

}
