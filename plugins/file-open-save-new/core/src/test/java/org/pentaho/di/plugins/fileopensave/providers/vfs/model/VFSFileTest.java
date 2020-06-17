/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.vfs.model;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.connections.vfs.provider.ConnectionFileProvider;

public class VFSFileTest {

  @Test
  public void getConnectionPathTest() {
    VFSFile file = new VFSFile();
    file.setPath( "ts://bucket/to/file/file.txt" );
    file.setConnection( "Connection Name" );
    Assert.assertEquals( ConnectionFileProvider.SCHEME + "://" + file.getConnection() + "/bucket/to/file/file.txt",
      file.getConnectionPath() );
    System.out.println( file.getConnectionPath() );
  }

  @Test
  public void getConnectionPathRootTest() {
    VFSFile file = new VFSFile();
    file.setPath( "ts://bucket" );
    file.setConnection( "Connection Name" );
    Assert.assertEquals( ConnectionFileProvider.SCHEME + "://" + file.getConnection() + "/bucket",
      file.getConnectionPath() );
  }

  @Test
  public void getConnectionPathWithDomainTest() {
    VFSFile file = new VFSFile();
    file.setPath( "ts://fake.com/path/to/file/file.txt" );
    file.setConnection( "Connection Name" );
    file.setDomain( "fake.com" );
    Assert.assertEquals( ConnectionFileProvider.SCHEME + "://" + file.getConnection() + "/path/to/file/file.txt",
      file.getConnectionPath() );

  }

  @Test
  public void getConnectionPathWithDomainRootTest() {
    VFSFile file = new VFSFile();
    file.setPath( "ts://fake.com" );
    file.setConnection( "Connection Name" );
    file.setDomain( "fake.com" );
    Assert.assertEquals( ConnectionFileProvider.SCHEME + "://" + file.getConnection(), file.getConnectionPath() );
  }

}
