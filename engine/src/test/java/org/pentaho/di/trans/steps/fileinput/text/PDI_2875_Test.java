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

package org.pentaho.di.trans.steps.fileinput.text;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for PDI-2875
 *
 * @author Pavel Sakun
 */
public class PDI_2875_Test {
  private static StepMockHelper<TextFileInputMeta, TextFileInputData> smh;
  private final String VAR_NAME = "VAR";
  private final String EXPRESSION = "${" + VAR_NAME + "}";

  @BeforeClass
  public static void setUp() throws KettleException {
    KettleEnvironment.init();
    smh =
      new StepMockHelper<TextFileInputMeta, TextFileInputData>( "CsvInputTest", TextFileInputMeta.class, TextFileInputData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  private TextFileInputMeta getMeta() {
    TextFileInputMeta meta = new TextFileInputMeta();
    meta.allocateFiles( 2 );
    meta.setFileName( new String[]{ "file1.txt",  "file2.txt" } );
    meta.inputFiles.includeSubFolders = new String[] { "n", "n" };
    meta.setFilter( new TextFileFilter[0] );
    meta.content.fileFormat =  "unix";
    meta.content.fileType = "CSV";
    meta.errorHandling.lineNumberFilesDestinationDirectory = EXPRESSION;
    meta.errorHandling.errorFilesDestinationDirectory =  EXPRESSION;
    meta.errorHandling.warningFilesDestinationDirectory = EXPRESSION;

    return meta;
  }

  @Test
  public void testVariableSubstitution() {
    doReturn( new Date() ).when( smh.trans ).getCurrentDate();
    TextFileInput step = spy( new TextFileInput( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );
    TextFileInputData data = new TextFileInputData();
    step.setVariable( VAR_NAME, "value" );
    step.init( getMeta(), data );
    verify( step, times( 2 ) ).environmentSubstitute( EXPRESSION );
  }
}
