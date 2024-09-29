/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.orabulkloader;

import org.apache.commons.vfs2.provider.local.LocalFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class OraBulkDataOutputTest {
  private static final String loadMethod = "AUTO_END";
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private OraBulkLoaderMeta oraBulkLoaderMeta;
  private OraBulkDataOutput oraBulkDataOutput;
  private Process sqlldrProcess;
  private VariableSpace space;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    String recTerm = Const.CR;
    sqlldrProcess = mock( Process.class );
    space = mock( VariableSpace.class );
    oraBulkLoaderMeta = mock( OraBulkLoaderMeta.class );
    oraBulkDataOutput = spy( new OraBulkDataOutput( oraBulkLoaderMeta, recTerm ) );

    when( oraBulkLoaderMeta.getLoadMethod() ).thenReturn( loadMethod );
    when( oraBulkLoaderMeta.getEncoding() ).thenReturn( null );
  }

  @Test
  public void testOpen() {
    try {
      String tmpDir = System.getProperty("java.io.tmpdir");
      File tempFile = File.createTempFile("orafiles", "test" );
      String tempFilePath = tempFile.getAbsolutePath();
      String dataFileVfsPath = "file:///" + tempFilePath;
      LocalFile tempFileObject = mock( LocalFile.class );

      tempFile.deleteOnExit();

      doReturn( dataFileVfsPath ).when( oraBulkLoaderMeta ).getDataFile();
      doReturn( tempFilePath ).when( space ).environmentSubstitute( dataFileVfsPath );
      doReturn( tempFileObject ).when( oraBulkDataOutput ).getFileObject( tempFilePath, space );
      doReturn( tempFilePath ).when( oraBulkDataOutput ).getFilename( tempFileObject );

      oraBulkDataOutput.open( space, sqlldrProcess );
      oraBulkDataOutput.close();

    } catch ( Exception ex ) {
      fail( "If any exception occurs, this test fails: " + ex );
    }
  }

  @Test
  public void testOpenFileException() {
    // Using thenAnswer() instead of thenThrow() as a workaround for new mockito exception behavior.
    // getDataFile doesn't actually throw this, and the place that could actually throw this in
    // OraBulkDataOutput.open() is difficult to mock
    when( oraBulkLoaderMeta.getDataFile() ).thenAnswer( i -> { throw new IOException(); } );
    try {
      oraBulkDataOutput.open( space, sqlldrProcess );
      fail( "An IOException was supposed to be thrown, failing test" );
    } catch ( KettleException kex ) {
      assertTrue( kex.getMessage().contains( "IO exception occured:" ) );
    }
  }
}
