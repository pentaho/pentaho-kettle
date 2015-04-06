package org.pentaho.di.trans.steps.transexecutor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TransExecutorMetaTest {

  LoadSaveTester loadSaveTester;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
        Arrays.asList( "fileName", "transName", "directoryPath", "groupSize", "groupField", "groupTime",
            "executionTimeField", "executionFilesRetrievedField", "executionLogTextField",
            "executionLogChannelIdField", "executionResultField", "executionNrErrorsField", "executionLinesReadField",
            "executionLinesWrittenField", "executionLinesInputField", "executionLinesOutputField",
            "executionLinesRejectedField", "executionLinesUpdatedField", "executionLinesDeletedField",
            "executionExitStatusField" );

    // executionResultTargetStepMeta -? (see for switch case meta)
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    loadSaveTester =
        new LoadSaveTester( TransExecutorMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }


  @Test
  public void firstStreamIsExecutionStatistics() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 0 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getExecutionResultTargetStepMeta() );
  }

  @Test
  public void secondStreamIsInternalTransformationsOutput() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 1 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getOutputRowsSourceStepMeta() );
  }

  @Test
  public void thirdStreamIsExecutionResultFiles() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 2 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getResultFilesTargetStepMeta() );
  }

  @Test
  public void forthStreamIsExecutorsInput() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 3 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getExecutorsOutputStepMeta() );
  }

  @Test
  public void testPrepareExecutionResultsFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    RowMetaInterface row = mock( RowMetaInterface.class );
    StepMeta nextStep = mock( StepMeta.class );

    meta.setExecutionResultTargetStepMeta( nextStep );
    meta.setExecutionTimeField( "time" );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

    meta.prepareExecutionResultsFields( row, nextStep );

    // make sure we get the name of the parent step meta... used for the origin step
    verify( parent ).getName();
    ArgumentCaptor<ValueMetaInterface> argumentCaptor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( row ).addValueMeta( argumentCaptor.capture() );
    assertEquals( "parent step", argumentCaptor.getValue().getOrigin() );
  }

  @Test
  public void testPrepareExecutionResultsFileFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    RowMetaInterface row = mock( RowMetaInterface.class );
    StepMeta nextStep = mock( StepMeta.class );

    meta.setResultFilesTargetStepMeta( nextStep );
    meta.setResultFilesFileNameField( "file_name" );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

    meta.prepareExecutionResultsFileFields( row, nextStep );

    // make sure we get the name of the parent step meta... used for the origin step
    verify( parent ).getName();
    ArgumentCaptor<ValueMetaInterface> argumentCaptor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( row ).addValueMeta( argumentCaptor.capture() );
    assertEquals( "parent step", argumentCaptor.getValue().getOrigin() );
  }

  @Test
  public void testPrepareResultsRowsFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    String[] outputFieldNames = new String[] { "one", "two" };
    int[] outputFieldTypes = new int[] { 0, 1 };
    int[] outputFieldLength = new int[] { 4, 8 };
    int[] outputFieldPrecision = new int[] { 2, 4 };

    meta.setOutputRowsField( outputFieldNames );
    meta.setOutputRowsType( outputFieldTypes );
    meta.setOutputRowsLength( outputFieldLength );
    meta.setOutputRowsPrecision( outputFieldPrecision );
    meta = spy( meta );

    RowMetaInterface row = mock( RowMetaInterface.class );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

    meta.prepareResultsRowsFields( row );

    // make sure we get the name of the parent step meta... used for the origin step
    verify( parent, times ( outputFieldNames.length ) ).getName();
    ArgumentCaptor<ValueMetaInterface> argumentCaptor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( row, times( outputFieldNames.length ) ).addValueMeta( argumentCaptor.capture() );
    assertEquals( "parent step", argumentCaptor.getValue().getOrigin() );
  }


  @SuppressWarnings( "unchecked" )
  private static StepIOMetaInterface mockStepIo( StreamInterface stream, int desiredIndex ) {
    List<StreamInterface> list = mock( List.class );
    when( list.indexOf( stream ) ).thenReturn( desiredIndex );
    when( list.get( eq( desiredIndex ) ) ).thenReturn( stream );

    StepIOMetaInterface stepIo = mock( StepIOMetaInterface.class );
    when( stepIo.getTargetStreams() ).thenReturn( list );
    return stepIo;
  }

  private static StreamInterface mockStream() {
    StepMeta stepMeta = mock( StepMeta.class );
    StreamInterface stream = mock( StreamInterface.class );
    when( stream.getStepMeta() ).thenReturn( stepMeta );
    return stream;
  }
}
