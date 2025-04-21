/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

import org.junit.Assert;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.w3c.dom.Node;

/**
 * SharedObjects tests
 * 
 * @author Yury Bakhmutski
 * @see SharedObjects
 */

public class SharedObjectsTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
    verify( sharedObjectsMock ).writeToFile( any(), anyString() );
  }

  @Test
  public void testCopyBackupVfs() throws Exception {
    final String dirName = "ram:/SharedObjectsTest";

    FileObject baseDir = KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( dirName );
    try {
      baseDir.createFolder();
      final String fileName = dirName + "/shared.xml";
      SharedObjects sharedObjects = new SharedObjects( fileName );

      SharedObjectInterface<?> shared1 = new TestSharedObject( "shared1", "<shared1>shared1</shared1>" );
      sharedObjects.storeObject( shared1 );
      sharedObjects.saveToFile();
      final String backupFileName = fileName + ".backup";
      FileObject backup = KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( backupFileName );
      Assert.assertFalse( backup.exists() );

      String contents = KettleVFS.getInstance( DefaultBowl.getInstance() ).getTextFileContent( fileName, "utf8" );
      Assert.assertTrue( contents.contains( shared1.getXML() ) );

      SharedObjectInterface<?> shared2 = new TestSharedObject( "shared2", "<shared2>shared2</shared2>" );
      sharedObjects.storeObject( shared2 );
      sharedObjects.saveToFile();
      Assert.assertTrue( backup.exists() );
      String contentsBackup = KettleVFS.getInstance( DefaultBowl.getInstance() ).getTextFileContent( backupFileName, "utf8" );
      Assert.assertEquals( contents, contentsBackup );

    } finally {
      if ( baseDir.exists() ) {
        baseDir.deleteAll();
      }
    }

  }

  private static class TestSharedObject extends SharedObjectBase implements SharedObjectInterface<TestSharedObject> {

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

    @Override
    public TestSharedObject makeClone() {
      return null;
    }

    @Override
    public Node toNode() throws KettleException {
      return null;
    }
  }

}
