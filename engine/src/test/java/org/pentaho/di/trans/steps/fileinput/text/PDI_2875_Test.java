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


package org.pentaho.di.trans.steps.fileinput.text;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
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
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUp() throws KettleException {
    KettleEnvironment.init();
    smh =
      new StepMockHelper<>( "CsvInputTest", TextFileInputMeta.class, TextFileInputData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void cleanUp() {
    smh.cleanUp();
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
