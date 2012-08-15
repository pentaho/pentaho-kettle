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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.FileProvider;
import org.junit.Test;
import org.pentaho.hadoop.shim.api.MockFileProvider;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;

public class ActiveHadoopShimFileProviderTest {

  @Test(expected = NullPointerException.class)
  public void instantiation_null_HadoopConfigurationFileSystemManager() {
    new ActiveHadoopShimFileProvider(null, "scheme");
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_scheme() {
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(
        new MockHadoopConfigurationProvider(), new DefaultFileSystemManager());
    new ActiveHadoopShimFileProvider(fsm, null);
  }

  private HadoopConfigurationFileSystemManager createTestFileSystemManager(HadoopConfiguration config, String scheme,
      FileProvider provider) throws FileSystemException {
    DefaultFileSystemManager def = new DefaultFileSystemManager();
    HadoopConfigurationProvider configProvider = new MockHadoopConfigurationProvider(Arrays.asList(config),
        config.getIdentifier());
    HadoopConfigurationFileSystemManager fsm = new HadoopConfigurationFileSystemManager(configProvider, def);
    fsm.addProvider(config, scheme, config.getIdentifier(), provider);
    return fsm;
  }

  @Test
  public void createFileSystem() throws FileSystemException {
    final AtomicBoolean called = new AtomicBoolean(false);
    String scheme = "scheme";
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim(), null, null, null);
    FileProvider provider = new MockFileProvider() {
      @Override
      public FileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions)
          throws FileSystemException {
        called.set(true);
        return null;
      }
    };

    HadoopConfigurationFileSystemManager fsm = createTestFileSystemManager(config, scheme, provider);
    ActiveHadoopShimFileProvider p = new ActiveHadoopShimFileProvider(fsm, scheme);
    p.createFileSystem(scheme, null, null);

    assertTrue("Expected provider method not called", called.get());
  }

  @Test
  public void findFile() throws FileSystemException {
    final AtomicBoolean called = new AtomicBoolean(false);
    String scheme = "scheme";
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim(), null, null, null);
    FileProvider provider = new MockFileProvider() {
      @Override
      public FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions)
          throws FileSystemException {
        called.set(true);
        return null;
      }
    };

    HadoopConfigurationFileSystemManager fsm = createTestFileSystemManager(config, scheme, provider);
    ActiveHadoopShimFileProvider p = new ActiveHadoopShimFileProvider(fsm, scheme);
    p.findFile(null, null, null);

    assertTrue("Expected provider method not called", called.get());
  }
  
  @Test
  public void getCapabilities() throws FileSystemException {
    final AtomicBoolean called = new AtomicBoolean(false);
    String scheme = "scheme";
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim(), null, null, null);
    FileProvider provider = new MockFileProvider() {
      @Override
      public Collection<Capability> getCapabilities() {
        called.set(true);
        return null;
      }
    };
    
    HadoopConfigurationFileSystemManager fsm = createTestFileSystemManager(config, scheme, provider);
    ActiveHadoopShimFileProvider p = new ActiveHadoopShimFileProvider(fsm, scheme);
    p.getCapabilities();
    
    assertTrue("Expected provider method not called", called.get());
  }
  
  @Test
  public void getConfigBuilder() throws FileSystemException {
    final AtomicBoolean called = new AtomicBoolean(false);
    String scheme = "scheme";
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim(), null, null, null);
    FileProvider provider = new MockFileProvider() {
      @Override
      public FileSystemConfigBuilder getConfigBuilder() {
        called.set(true);
        return null;
      }
    };
    
    HadoopConfigurationFileSystemManager fsm = createTestFileSystemManager(config, scheme, provider);
    ActiveHadoopShimFileProvider p = new ActiveHadoopShimFileProvider(fsm, scheme);
    p.getConfigBuilder();
    
    assertTrue("Expected provider method not called", called.get());
  }
  
  @Test
  public void parseUri() throws FileSystemException {
    final AtomicBoolean called = new AtomicBoolean(false);
    String scheme = "scheme";
    HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim(), null, null, null);
    FileProvider provider = new MockFileProvider() {
      @Override
      public FileName parseUri(FileName root, String uri) throws FileSystemException {
        called.set(true);
        return null;
      }
    };
    
    HadoopConfigurationFileSystemManager fsm = createTestFileSystemManager(config, scheme, provider);
    ActiveHadoopShimFileProvider p = new ActiveHadoopShimFileProvider(fsm, scheme);
    p.parseUri(null, null);
    
    assertTrue("Expected provider method not called", called.get());
  }

}
