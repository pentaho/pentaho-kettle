/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;

public class VFSFileTest {

  @Test
  public void getConnectionPathTest() {
    VFSFile file = new VFSFile();
    String pvfsPath = "pvfs://someConnectionName/bucket/to/file/file.txt";
    file.setPath( pvfsPath );
    file.setConnection( "someConnectionName" );
    Assert.assertEquals( pvfsPath, file.getConnectionPath() );
  }

  @Test
  public void getConnectionPathRootTest() {
    VFSFile file = new VFSFile();
    String pvfsPath = "pvfs://someConnectionName/bucket";
    file.setPath( pvfsPath );
    file.setConnection( "someConnectionName" );
    Assert.assertEquals( pvfsPath, file.getConnectionPath() );
  }

  @Test
  public void getConnectionPathWithDomainTest() {
    VFSFile file = new VFSFile();
    String pvfsPath = "pvfs://someConnectionName/path/to/file/file.txt";
    file.setPath( pvfsPath );
    file.setConnection( "someConnectionName" );
    file.setDomain( "fake.com" );
    Assert.assertEquals( pvfsPath, file.getConnectionPath() );

  }

  @Test
  public void getConnectionPathWithDomainRootTest() {
    VFSFile file = new VFSFile();
    String pvfsPath = "pvfs://someConnectionName";
    file.setPath( pvfsPath );
    file.setConnection( "someConnectionName" );
    file.setDomain( "fake.com" );
    Assert.assertEquals( pvfsPath, file.getConnectionPath() );
  }

  @Test
  public void getNameDecodedTest() {
    VFSFile file = new VFSFile();
    String name = "some%25Connection%25Name";
    file.setName( name );
    Assert.assertEquals( "some%Connection%Name", file.getNameDecoded() );
  }

}
