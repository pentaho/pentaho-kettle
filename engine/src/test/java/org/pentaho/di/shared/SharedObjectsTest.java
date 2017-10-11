/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.shared;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

import org.junit.Assert;

/**
 * SharedObjects tests
 * 
 * @author Yury Bakhmutski
 * @see SharedObjects
 */
public class SharedObjectsTest {

  @Mock
  SharedObjects sharedObjectsMock;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks( this );
  }

  @Test
  public void writeToFileTest() throws KettleException, IOException {
    doCallRealMethod().when( sharedObjectsMock ).writeToFile( any( FileObject.class ), anyString() );

    when( sharedObjectsMock.initOutputStreamUsingKettleVFS( any( FileObject.class ) ) ).thenThrow(
        new RuntimeException() );

    try {
      sharedObjectsMock.writeToFile( any( FileObject.class ), anyString() );
    } catch ( KettleException e ) {
      // NOP: catch block throws an KettleException after calling sharedObjectsMock method
    }

    // check if file restored in case of exception is occurred
    verify( sharedObjectsMock ).restoreFileFromBackup( anyString() );
  }

  @Test
  public void testCopyBackupVfs() throws Exception {
    final String dirName = "ram:/SharedObjectsTest";

    FileObject baseDir = KettleVFS.getFileObject( dirName );
    try {
      baseDir.createFolder();
      final String fileName = dirName + "/shared.xml";
      SharedObjects sharedObjects = new SharedObjects( fileName );

      SharedObjectInterface shared1 = new TestSharedObject( "shared1", "<shared1>shared1</shared1>" );
      sharedObjects.storeObject( shared1 );
      sharedObjects.saveToFile();
      final String backupFileName = fileName + ".backup";
      FileObject backup = KettleVFS.getFileObject( backupFileName );
      Assert.assertFalse( backup.exists() );

      String contents = KettleVFS.getTextFileContent( fileName, "utf8" );
      Assert.assertTrue( contents.contains( shared1.getXML() ) );

      SharedObjectInterface shared2 = new TestSharedObject( "shared2", "<shared2>shared2</shared2>" );
      sharedObjects.storeObject( shared2 );
      sharedObjects.saveToFile();
      Assert.assertTrue( backup.exists() );
      String contentsBackup = KettleVFS.getTextFileContent( backupFileName, "utf8" );
      Assert.assertEquals( contents, contentsBackup );

    } finally {
      if ( baseDir.exists() ) {
        baseDir.deleteAll();
      }
    }

  }

  private static class TestSharedObject extends SharedObjectBase implements SharedObjectInterface {

    private String name, xml;

    public TestSharedObject( String name, String xml ) {
      this.name = name;
      this.xml = xml;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getXML() throws KettleException {
      return xml;
    }

  }

}
