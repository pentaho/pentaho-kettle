package org.pentaho.di.engine.kettlenative.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EngineITest {

  TransMeta testMeta;
  static Engine engine = new Engine();

  @Before
  public void before() throws KettleException {
    KettleEnvironment.init();
    testMeta = new TransMeta( getClass().getClassLoader().getResource( "test2.ktr" ).getFile() );
  }

  @Test
  public void testExec() throws KettleXMLException, KettleMissingPluginsException, InterruptedException {
    TransMeta meta = new TransMeta( getClass().getClassLoader().getResource( "lorem.ktr" ).getFile() );
    ITransformation trans = Transformation.convert( meta );
    IExecutionContext executionContext = engine.prepare( trans );
    engine.execute( executionContext );
  }

  @Test
  public void test2Sources1Sink()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "2InputsWithConsistentColumns.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    assertThat( reports.size(), is( 3 ) );
    IExecutableOperation dataGrid1 = getByName( "Data Grid", reports );
    IExecutableOperation dataGrid2 = getByName( "Data Grid 2", reports );
    IExecutableOperation dummy = getByName( "Dummy (do nothing)", reports );
    System.out.println( reports );
    assertThat( dataGrid1.getOut(), is( 1l ) );
    assertThat( dataGrid2.getOut(), is( 1l ) );
    assertThat( "dummy should get rows fromm both data grids", dummy.getIn(), is( 2l ) );
    System.out.println( reports );
  }

  @Test
  public void test1source2trans1sink()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "1source.2Trans.1sink.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    assertThat( reports.size(), is( 5 ) );
    System.out.println( reports );

  }

  @Test @Ignore // Missing KTR?
  public void simpleFilter()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "simpleFilter.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    System.out.println( reports );

  }

  @Test
  public void testLookup()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "SparkSample.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    Thread.sleep( 100 );  // Don't check before file is done being written
    assertThat(
      reports.stream()
        .filter( isOp( "Merged Output" ) )
        .findFirst()
        .get()
        .getOut(),
      is( 2001l ) );  // hmm, out + written
    System.out.println( reports );
  }

  @Test
  public void testChainedCalc()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    // executes a series of Calculation steps as a chain of Spark FlatMapFunctions.
    IExecutionResult result = getTestExecutionResult( "StringCalc.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    System.out.println( reports );

  }


  private Predicate<? super IProgressReporting<IDataEvent>> isOp( String s ) {
    return o -> o.getId().equals( s );
  }


  private IExecutionResult getTestExecutionResult( String transName )
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException,
    ExecutionException {
    TransMeta meta = new TransMeta( getClass().getClassLoader().getResource( transName ).getFile() );
    ITransformation trans = Transformation.convert( meta );
    IExecutionContext executionContext = engine.prepare( trans );
    Future<IExecutionResult> resultFuture = engine.execute( executionContext );
    return resultFuture.get();
  }

  private IExecutableOperation getByName( String name, List<IProgressReporting<IDataEvent>> reports ) {
    return reports.stream()
      .map( IExecutableOperation.class::cast )
      .filter( report -> report.getId().equals( name ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException() );
  }


}