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

package org.pentaho.di.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;

public class ResultFileTest {

  @Test
  public void testGetRow() throws KettleFileException, FileSystemException {
    File tempDir = new File( new TemporaryFolder().toString() );
    FileObject tempFile = KettleVFS.createTempFile( "prefix", "suffix", tempDir.toString() );
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
