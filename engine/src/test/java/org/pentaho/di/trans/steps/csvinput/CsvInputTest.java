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
import java.io.InputStreamReader;
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
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.vfs.KettleVFSImpl;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareStep;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;
import org.pentaho.di.trans.steps.fileinput.text.TextFileLine;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
  @SuppressWarnings( "java:S1874" )
  // CsvInput uses deprecated class TextFileInput & KettleVFS.getFileObject to read data from file
  public void testGetFieldsAction() throws Exception {
    List<ValueMetaInterface> valueMetaList = new ArrayList<>();
    valueMetaList.add( ValueMetaFactory.createValueMeta( "field1", ValueMetaInterface.TYPE_STRING ) );
    valueMetaList.add( ValueMetaFactory.createValueMeta( "field2", ValueMetaInterface.TYPE_NUMBER ) );
    String[] fieldsData = { "field1", "field2", "field3" };
    String sampleData = "1,name,3.14159,city,1954/02/07,145.00,'1,111,111.1',1.234E0,+123,ALASKA";
    TextFileLine textFileLine = new TextFileLine( sampleData, 1, null );
    TextFileInputField[] inputFileFields = createInputFileFields( "f1", "f2", "f3" );
    String fileContents = "Something" + DELIMITER + DELIMITER + "The former was empty!";
    File tmpFile = createTestFile( ENCODING, fileContents );

    CsvInputAwareStep csvInputAwareStep = mock( CsvInputAwareStep.class );
    CsvInputData data = mock( CsvInputData.class );
    CsvInputMeta meta = createMeta( tmpFile, inputFileFields );
    meta.setDefault();
    meta.setEnclosure( "'" );

    CsvInput csvInput = spy(
      new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );
    csvInput.init( meta, data );

    FileObject fileObject = Mockito.mock( FileObject.class );
    RowMetaInterface outputRowMeta = Mockito.mock( RowMeta.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    InputStreamReader inputStreamReader =
      new InputStreamReader( new ByteArrayInputStream( sampleData.getBytes() ) );
    BufferedInputStreamReader bufferedInputStreamReader = new BufferedInputStreamReader( inputStreamReader );

    try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = Mockito.mockStatic( KettleVFS.class );
          MockedStatic<TextFileInputUtils> textFileInputUtilsStatic = Mockito.mockStatic(
            TextFileInputUtils.class ) ) {

      KettleVFSImpl vfsImpl = mock( KettleVFSImpl.class );
      kettleVFSMockedStatic.when( () -> KettleVFS.getInstance( any( Bowl.class ) ) ).thenReturn( vfsImpl );

      when( vfsImpl.getFileObject( anyString() ) ).thenReturn( fileObject );
      kettleVFSMockedStatic.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
        .thenReturn( inputStream );
      textFileInputUtilsStatic.when(
          () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(),
            any(),
            any() ) )
        .thenReturn( sampleData );
      textFileInputUtilsStatic.when(
          () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(),
            any(),
            any(), anyLong() ) )
        .thenReturn( textFileLine );

      when( outputRowMeta.getValueMetaList() ).thenReturn( valueMetaList );
      when( csvInputAwareStep.getFieldNames( any( CsvInputAwareMeta.class ) ) ).thenReturn( fieldsData );
      when(
        csvInputAwareStep.getBufferedReader( any( CsvInputAwareMeta.class ), any( InputStream.class ) ) ).thenReturn(
        bufferedInputStreamReader );
      when( stepMockHelper.transMeta.environmentSubstitute( (String[]) any() ) ).thenReturn( new String[] {} );
      when( stepMockHelper.transMeta.environmentSubstitute( meta.getDelimiter() ) ).thenReturn( meta.getDelimiter() );
      when( stepMockHelper.transMeta.environmentSubstitute( meta.getEnclosure() ) ).thenReturn( meta.getEnclosure() );
      when( stepMockHelper.transMeta.environmentSubstitute( meta.getFilename() ) ).thenReturn( meta.getFilename() );
      when( stepMockHelper.transMeta.environmentSubstitute( meta.getEscapeCharacter() ) ).thenReturn(
        meta.getEscapeCharacter() );

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put( "noOfFields", "10" );
      queryParams.put( "isSampleSummary", "true" );
      JSONObject response =
        csvInput.doAction( "getFields", meta, null, null, queryParams );

      assertNotNull( response.get( "fields" ) );
      assertNotNull( response.get( "summary" ) );
    }
  }

}
