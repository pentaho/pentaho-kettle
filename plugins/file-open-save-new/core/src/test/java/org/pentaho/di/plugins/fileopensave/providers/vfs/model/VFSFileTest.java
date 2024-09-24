/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
