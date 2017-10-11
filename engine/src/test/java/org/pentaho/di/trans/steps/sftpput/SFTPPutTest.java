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

package org.pentaho.di.trans.steps.sftpput;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.trans.steps.StepMockUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * @author Andrey Khayrutdinov
 */
public class SFTPPutTest {

  private SFTPPut step;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    SFTPClient clientMock = mock( SFTPClient.class );

    step = StepMockUtil.getStep( SFTPPut.class, SFTPPutMeta.class, "mock step" );
    step = spy( step );
    doReturn( clientMock ).when( step )
      .createSftpClient( anyString(), anyString(), anyString(), anyString(), anyString() );
  }


  private static RowMeta rowOfStringsMeta( String... columns ) {
    RowMeta rowMeta = new RowMeta();
    for ( String column : columns ) {
      rowMeta.addValueMeta( new ValueMetaString( column ) );
    }
    return rowMeta;
  }


  @Test
  public void checkRemoteFilenameField_FieldNameIsBlank() throws Exception {
    SFTPPutData data = new SFTPPutData();
    step.checkRemoteFilenameField( "", data );
    assertEquals( -1, data.indexOfSourceFileFieldName );
  }

  @Test( expected = KettleStepException.class )
  public void checkRemoteFilenameField_FieldNameIsSet_NotFound() throws Exception {
    step.setInputRowMeta( new RowMeta() );
    step.checkRemoteFilenameField( "remoteFileName", new SFTPPutData() );
  }

  @Test
  public void checkRemoteFilenameField_FieldNameIsSet_Found() throws Exception {
    RowMeta rowMeta = rowOfStringsMeta( "some field", "remoteFileName" );
    step.setInputRowMeta( rowMeta );

    SFTPPutData data = new SFTPPutData();
    step.checkRemoteFilenameField( "remoteFileName", data );
    assertEquals( 1, data.indexOfRemoteFilename );
  }


  @Test( expected = KettleStepException.class )
  public void checkSourceFileField_NameIsBlank() throws Exception {
    SFTPPutData data = new SFTPPutData();
    step.checkSourceFileField( "", data );
  }

  @Test( expected = KettleStepException.class )
  public void checkSourceFileField_NameIsSet_NotFound() throws Exception {
    step.setInputRowMeta( new RowMeta() );
    step.checkSourceFileField( "sourceFile", new SFTPPutData() );
  }

  @Test
  public void checkSourceFileField_NameIsSet_Found() throws Exception {
    RowMeta rowMeta = rowOfStringsMeta( "some field", "sourceFileFieldName" );
    step.setInputRowMeta( rowMeta );

    SFTPPutData data = new SFTPPutData();
    step.checkSourceFileField( "sourceFileFieldName", data );
    assertEquals( 1, data.indexOfSourceFileFieldName );
  }


  @Test( expected = KettleStepException.class )
  public void checkRemoteFoldernameField_NameIsBlank() throws Exception {
    SFTPPutData data = new SFTPPutData();
    step.checkRemoteFoldernameField( "", data );
  }

  @Test( expected = KettleStepException.class )
  public void checkRemoteFoldernameField_NameIsSet_NotFound() throws Exception {
    step.setInputRowMeta( new RowMeta() );
    step.checkRemoteFoldernameField( "remoteFolder", new SFTPPutData() );
  }

  @Test
  public void checkRemoteFoldernameField_NameIsSet_Found() throws Exception {
    RowMeta rowMeta = rowOfStringsMeta( "some field", "remoteFoldernameFieldName" );
    step.setInputRowMeta( rowMeta );

    SFTPPutData data = new SFTPPutData();
    step.checkRemoteFoldernameField( "remoteFoldernameFieldName", data );
    assertEquals( 1, data.indexOfRemoteDirectory );
  }


  @Test( expected = KettleStepException.class )
  public void checkDestinationFolderField_NameIsBlank() throws Exception {
    SFTPPutData data = new SFTPPutData();
    step.checkDestinationFolderField( "", data );
  }

  @Test( expected = KettleStepException.class )
  public void checkDestinationFolderField_NameIsSet_NotFound() throws Exception {
    step.setInputRowMeta( new RowMeta() );
    step.checkDestinationFolderField( "destinationFolder", new SFTPPutData() );
  }

  @Test
  public void checkDestinationFolderField_NameIsSet_Found() throws Exception {
    RowMeta rowMeta = rowOfStringsMeta( "some field", "destinationFolderFieldName" );
    step.setInputRowMeta( rowMeta );

    SFTPPutData data = new SFTPPutData();
    step.checkDestinationFolderField( "destinationFolderFieldName", data );
    assertEquals( 1, data.indexOfMoveToFolderFieldName );
  }


  @Test
  public void remoteFilenameFieldIsMandatoryWhenStreamingFromInputField() throws Exception {
    RowMeta rowMeta = rowOfStringsMeta( "sourceFilenameFieldName", "remoteDirectoryFieldName" );
    step.setInputRowMeta( rowMeta );

    doReturn( new Object[] { "qwerty", "asdfg" } ).when( step ).getRow();

    SFTPPutMeta meta = new SFTPPutMeta();
    meta.setInputStream( true );
    meta.setPassword( "qwerty" );
    meta.setSourceFileFieldName( "sourceFilenameFieldName" );
    meta.setRemoteDirectoryFieldName( "remoteDirectoryFieldName" );

    step.processRow( meta, new SFTPPutData() );
    assertEquals( 1, step.getErrors() );
  }
}
