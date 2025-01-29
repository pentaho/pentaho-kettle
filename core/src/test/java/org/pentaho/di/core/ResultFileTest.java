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


package org.pentaho.di.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class ResultFileTest {

  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void testGetRow() throws KettleFileException, FileSystemException {
    File tempDir = new File( new TemporaryFolder().toString() );
    FileObject tempFile = KettleVFS.getInstance( DefaultBowl.getInstance() )
      .createTempFile( "prefix", "suffix", tempDir.toString() );
    Date timeBeforeFile = Calendar.getInstance().getTime();
    ResultFile resultFile = new ResultFile( ResultFile.FILE_TYPE_GENERAL, tempFile, "myOriginParent", "myOrigin" );
    Date timeAfterFile = Calendar.getInstance().getTime();

    assertNotNull( resultFile );
    RowMetaInterface rm = resultFile.getRow().getRowMeta();
    assertEquals( 7, rm.getValueMetaList().size() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 1 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 2 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 3 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 4 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 5 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_DATE, rm.getValueMeta( 6 ).getType() );

    assertEquals( ResultFile.FILE_TYPE_GENERAL, resultFile.getType() );
    assertEquals( "myOrigin", resultFile.getOrigin() );
    assertEquals( "myOriginParent", resultFile.getOriginParent() );
    assertTrue( "ResultFile timestamp is created in the expected window",
      timeBeforeFile.compareTo( resultFile.getTimestamp() ) <= 0
      && timeAfterFile.compareTo( resultFile.getTimestamp() ) >= 0 );

    tempFile.delete();
    tempDir.delete();
  }
}
