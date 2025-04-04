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


package org.pentaho.di.trans.steps.csvinput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareStep;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNotNull;

public class CsvInputTest extends CsvInputUnitTestBase {

  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;
  private LogChannelInterface logChannelInterface;
  private CsvInputMeta csvInputMeta;

  @Before
  public void setUp() throws Exception {
    logChannelInterface = mock( LogChannelInterface.class );
    stepMockHelper = StepMockUtil
      .getStepMockHelper( CsvInputMeta.class, "CsvInputTest" );
    csvInputMeta = mock( CsvInputMeta.class );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void guessStringsFromLineWithEmptyLine() throws Exception {
    // This only validates that, given a null 'line', a null is returned!
    String[] saData = CsvInput.guessStringsFromLine( logChannelInterface, null, csvInputMeta.getDelimiter(),
      csvInputMeta.getEnclosure(), csvInputMeta.getEscapeCharacter() );

    assertNull( saData );
  }

  // PDI-17831
  @Test
  public void testFileIsReleasedAfterProcessing() throws Exception {
    // Create a file with some content to be processed
    TextFileInputField[] inputFileFields = createInputFileFields( "f1", "f2", "f3" );
    String fileContents = "Something" + DELIMITER + DELIMITER + "The former was empty!";
    File tmpFile = createTestFile( ENCODING, fileContents );

    // Create and configure the step
    CsvInputMeta meta = createMeta( tmpFile, inputFileFields );
    CsvInputData data = new CsvInputData();
    RowSet output = new QueueRowSet();
    CsvInput csvInput =
      new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    csvInput.init( meta, data );
    csvInput.addRowSetToOutputRowSets( output );

    // Start processing
    csvInput.processRow( meta, data );

    // Finish processing
    csvInput.dispose( meta, data );

    // And now the file must be free to be deleted
    assertTrue( tmpFile.delete() );
    assertFalse( tmpFile.exists() );
  }

  @Test
  public void testFilenameValidatorForInputFilesConnectedToRep() {
    CsvInput csvInput = mock( CsvInput.class );

    String internalEntryVariable = "internalEntryVariable";
    String internalTransformationVariable = "internalTransformationVariable";

    String filename = Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ;
    csvInput.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, internalEntryVariable );
    csvInput.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, internalTransformationVariable );

    TransMeta transmeta = mock( TransMeta.class );

    Repository rep = mock( Repository.class );

    when( csvInput.getTransMeta() ).thenReturn( transmeta );
    when( transmeta.getRepository() ).thenReturn( rep );
    when( rep.isConnected() ).thenReturn( true );

    when( csvInput.filenameValidatorForInputFiles( any() ) ).thenCallRealMethod();
    when( csvInput.environmentSubstitute( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY  ) ).thenReturn( internalEntryVariable );
    when( csvInput.environmentSubstitute( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY  ) ).thenReturn( internalTransformationVariable );


    String finalFilename = csvInput.filenameValidatorForInputFiles(filename);

    assertEquals( internalTransformationVariable, finalFilename );

  }

  @Test
  public void testFilenameValidatorForInputFilesNotConnectedToRep() {
    CsvInput csvInput = mock( CsvInput.class );

    String internalEntryVariable = "internalEntryVariable";
    String internalTransformationVariable = "internalTransformationVariable";

    String filename = Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ;
    csvInput.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, internalEntryVariable );
    csvInput.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, internalTransformationVariable );

    TransMeta transmeta = mock( TransMeta.class );

    Repository rep = mock( Repository.class );

    when( csvInput.getTransMeta() ).thenReturn( transmeta );
    when( transmeta.getRepository() ).thenReturn( rep );
    when( rep.isConnected() ).thenReturn( false );

    when( csvInput.filenameValidatorForInputFiles( any() ) ).thenCallRealMethod();
    when( csvInput.environmentSubstitute( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY  ) ).thenReturn( internalEntryVariable );
    when( csvInput.environmentSubstitute( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY  ) ).thenReturn( internalTransformationVariable );


    String finalFilename = csvInput.filenameValidatorForInputFiles(filename);

    assertEquals( internalEntryVariable, finalFilename );

  }

  @Test
  public void testGetFieldsAction() throws Exception {
    List<ValueMetaInterface> valueMetaList = new ArrayList<>();
    valueMetaList.add( ValueMetaFactory.createValueMeta( "field1", ValueMetaInterface.TYPE_STRING ) );
    valueMetaList.add( ValueMetaFactory.createValueMeta( "field2", ValueMetaInterface.TYPE_NUMBER ) );
    String[] fieldsData = { "field1", "field2" };
    Object[] rowData = { "value1", "value2" };

    CsvInputAwareMeta csvInputAwareMeta = mock( CsvInputAwareMeta.class );
    CsvInputAwareStep csvInputAwareStep = mock( CsvInputAwareStep.class );
    CsvInputMeta meta = mock( CsvInputMeta.class );
    CsvInputData data = mock( CsvInputData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    FileObject fileObject = Mockito.mock( FileObject.class );
    RowMetaInterface outputRowMeta = Mockito.mock( RowMeta.class );
    when( meta.getDelimiter() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.getEscapeCharacter() ).thenReturn( "\\" );
    when( meta.getEncoding() ).thenReturn( "UTF-8" );
    when( meta.getFileFormatTypeNr() ).thenReturn( 1 );
    when( meta.hasHeader() ).thenReturn( true );
    when( meta.getFileFormatTypeNr() ).thenReturn( 2 );
    when( meta.getBufferSize() ).thenReturn( "5000" );
    when( meta.getFilePaths( any( VariableSpace.class ) ) ).thenReturn( new String[] { "test/path/file.csv" } );

    CsvInput csvInput =
      new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    csvInput.init( meta, data );

    MockedStatic<KettleVFS> kettleVFSMockedStatic = Mockito.mockStatic( KettleVFS.class );
    MockedStatic<TextFileInputUtils> textFileInputUtilsStatic = Mockito.mockStatic( TextFileInputUtils.class );
    @SuppressWarnings( "java:S1874" )// CsvInput uses deprecated class TextFileInput to read data from file
    MockedStatic<TextFileInput> textFileInputStatic = Mockito.mockStatic( TextFileInput.class );

    kettleVFSMockedStatic.when( () -> KettleVFS.getFileObject( anyString() ) )
      .thenReturn( fileObject );
    kettleVFSMockedStatic.when( () -> KettleVFS.getInputStream( anyString() ) )
      .thenReturn( new ByteArrayInputStream( "".getBytes() ) );
    textFileInputUtilsStatic.when(
        () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(), any(),
          any() ) )
      .thenReturn( "test,sample,line" );
    textFileInputStatic.when(
      () -> TextFileInput.convertLineToRow( any(), any(), any(), any(), anyInt(), any(), any(), any(), anyLong(),
        any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(),
        anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), anyBoolean(), any(), any(), any(), any(),
        anyInt(), anyBoolean() ) ).thenReturn( rowData );

    when( csvInputAwareStep.getFieldNames( any( CsvInputAwareMeta.class ) ) ).thenReturn( fieldsData );
    when( csvInput.getInputStream( csvInputAwareMeta ) ).thenReturn( inputStream );
    when( stepMockHelper.transMeta.environmentSubstitute( (String[]) any() ) ).thenReturn( new String[] {} );
    when( meta.getHeaderFileObject( stepMockHelper.transMeta ) ).thenReturn( fileObject );
    when( KettleVFS.getInputStream( any( FileObject.class ) ) ).thenReturn( inputStream );
    when( KettleVFS.getFileObject( anyString() ) ).thenReturn( fileObject );
    when( outputRowMeta.getValueMetaList() ).thenReturn( valueMetaList );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "noOfFields", "10" );
    queryParams.put( "isSampleSummary", "true" );
    JSONObject response =
      csvInput.doAction( "getFields", meta, null, null, queryParams );

    assertNotNull( response.get( "fields" ) );
    assertNotNull( response.get( "summary" ) );
  }

}
