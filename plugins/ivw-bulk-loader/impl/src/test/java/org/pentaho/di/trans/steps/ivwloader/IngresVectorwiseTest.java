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


package org.pentaho.di.trans.steps.ivwloader;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * User: Dzmitry Stsiapanau Date: 11/20/13 Time: 12:41 PM
 */
public class IngresVectorwiseTest {

  private class IngresVectorwiseLoaderTest extends IngresVectorwiseLoader {
    // public List<Throwable> errors = new ArrayList<Throwable>();

    public IngresVectorwiseLoaderTest( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
      TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    /**
     * Create the command line for a sql process depending on the meta information supplied.
     * 
     * @param meta
     *          The meta data to create the command line from
     * @return The string to execute.
     * @throws org.pentaho.di.core.exception.KettleException
     *           Upon any exception
     */
    @Override
    public String createCommandLine( IngresVectorwiseLoaderMeta meta ) throws KettleException {

      String bufferSizeString = environmentSubstitute( meta.getBufferSize() );
      int bufferSize = Utils.isEmpty( bufferSizeString ) ? 5000 : Const.toInt( bufferSizeString, 5000 );

      Class<?> vwload = VWLoadMocker.class;
      String userDir;
      try {
        // note: we need to convert to URI to get a valid Path to use with windows
        userDir = Paths.get( vwload.getProtectionDomain().getCodeSource().getLocation().toURI() ).toString();
      } catch ( URISyntaxException e ) {
        throw new KettleException( e );
      }

      return "java -cp . -Duser.dir=" + userDir + ' '
        + vwload.getCanonicalName() + ' ' + bufferSize + ' ' + meta.getMaxNrErrors() + ' ' + meta.getErrorFileName();

    }
  }

  private static final String IVW_TEMP_PREFIX = "IngresVectorwiseLoaderTest";
  private static final String IVW_TEMP_EXTENSION = ".txt";
  private String lineSeparator = System.getProperty( "line.separator" );

  private static StepMockHelper<IngresVectorwiseLoaderMeta, IngresVectorwiseLoaderData> stepMockHelper;
  private String[] fieldStream = new String[] { "Number data", "String data" };
  private Object[] row = new Object[] { 1L, "another data" };
  private Object[] row2 = new Object[] { 2l, "another data2" };
  private Object[] row3 = new Object[] { 3l, "another data3" };
  private Object[] wrongRow = new Object[] { 10000L, "wrong data" };
  private List<Object[]> wrongRows = new ArrayList<Object[]>();
  private List<Object[]> rows = new ArrayList<Object[]>();

  {
    rows.add( row );
    rows.add( row2 );
    rows.add( row3 );

    wrongRows.add( row );
    wrongRows.add( wrongRow );
    wrongRows.add( row3 );

  }

  @BeforeClass
  public static void setUp() throws Exception {
    stepMockHelper =
      new StepMockHelper<IngresVectorwiseLoaderMeta, IngresVectorwiseLoaderData>( "INGRES_VECTORWISE_TEST",
        IngresVectorwiseLoaderMeta.class, IngresVectorwiseLoaderData.class );
    stepMockHelper.redirectLog( System.out, LogLevel.ROWLEVEL );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  /**
   * Test will fail if you're on a Windows system. IngresVectorwise Bulk Loading is only available for POSIX Operating
   * Systems:
   * "The VW bulk loader only runs under POSIX operating systems that support named pipes. This includes every modern
   * operating system besides Windows."
   * @see <a href="https://wiki.pentaho.com/display/EAI/Ingres+VectorWise+Bulk+Loader">Ingres Vectorwise Bulk Loader</a>
   */
  @Test
  public void testGuiErrors() {
    try {
      int r = wrongRows.size();
      BaseStep step = doOutput( wrongRows, "0" );
      ( (IngresVectorwiseLoader) step ).vwLoadMonitorThread.join();
      assertEquals( 0, step.getLinesOutput() );
      assertEquals( r, step.getLinesRead() );
      assertEquals( r, step.getLinesWritten() );
      assertEquals( 1, step.getLinesRejected() );
      assertEquals( 1, step.getErrors() );
    } catch ( KettleException e ) {
      fail( e.getMessage() );
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Test will fail if you're on a Windows system. IngresVectorwise Bulk Loading is only available for POSIX Operating
   * Systems:
   * "The VW bulk loader only runs under POSIX operating systems that support named pipes. This includes every modern
   * operating system besides Windows."
   * @see <a href="https://wiki.pentaho.com/display/EAI/Ingres+VectorWise+Bulk+Loader">Ingres Vectorwise Bulk Loader</a>
   */
  @Test
  public void testGuiErrorsWithErrorsAllowed() {
    try {
      int r = wrongRows.size();
      BaseStep step = doOutput( wrongRows, "2" );
      ( (IngresVectorwiseLoader) step ).vwLoadMonitorThread.join();
      assertEquals( r - 1, step.getLinesOutput() );
      assertEquals( r, step.getLinesRead() );
      assertEquals( r, step.getLinesWritten() );
      assertEquals( 1, step.getLinesRejected() );
      assertEquals( 0, step.getErrors() );
    } catch ( KettleException e ) {
      fail( e.getMessage() );
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Test will fail if you're on a Windows system. IngresVectorwise Bulk Loading is only available for POSIX Operating
   * Systems:
   * "The VW bulk loader only runs under POSIX operating systems that support named pipes. This includes every modern
   * operating system besides Windows."
   * @see <a href="https://wiki.pentaho.com/display/EAI/Ingres+VectorWise+Bulk+Loader">Ingres Vectorwise Bulk Loader</a>
   */
  @Test
  public void testGuiSuccess() {
    try {
      int r = rows.size();
      BaseStep step = doOutput( rows, "0" );
      ( (IngresVectorwiseLoader) step ).vwLoadMonitorThread.join();
      assertEquals( r, step.getLinesOutput() );
      assertEquals( r, step.getLinesRead() );
      assertEquals( r, step.getLinesWritten() );
      assertEquals( 0, step.getLinesRejected() );
      assertEquals( 0, step.getErrors() );
    } catch ( KettleException e ) {
      fail( e.getMessage() );
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Test will fail if you're on a Windows system. IngresVectorwise Bulk Loading is only available for POSIX Operating
   * Systems:
   * "The VW bulk loader only runs under POSIX operating systems that support named pipes. This includes every modern
   * operating system besides Windows."
   * @see <a href="https://wiki.pentaho.com/display/EAI/Ingres+VectorWise+Bulk+Loader">Ingres Vectorwise Bulk Loader</a>
   */
  @Test
  public void testWaitForFinish() {
    try {
      int r = rows.size();
      BaseStep step = doOutput( wrongRows, "2" );
      assertEquals( r - 1, step.getLinesOutput() );
      assertEquals( r, step.getLinesRead() );
      assertEquals( r, step.getLinesWritten() );
      assertEquals( 1, step.getLinesRejected() );
      assertEquals( 0, step.getErrors() );

    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }
  }

  /**
   * Test will fail if you're on a Windows system. IngresVectorwise Bulk Loading is only available for POSIX Operating
   * Systems:
   * "The VW bulk loader only runs under POSIX operating systems that support named pipes. This includes every modern
   * operating system besides Windows."
   * @see <a href="https://wiki.pentaho.com/display/EAI/Ingres+VectorWise+Bulk+Loader">Ingres Vectorwise Bulk Loader</a>
   */
  @Test
  @Ignore
  public void testVWLoadMocker() {
    String cmd =
      "java -cp . -Duser.dir=" + VWLoadMocker.class.getProtectionDomain().getCodeSource().getLocation().getPath()
        + " org.pentaho.di.trans.steps.ivwloader.VWLoadMocker 5000 0 /tmp/error.txt";
    // String cmd ="java -version";

    try {
      System.out.println( cmd );
      Process pr = Runtime.getRuntime().exec( cmd );

      pr.getOutputStream().write( ( "testVWLoadMaker" + lineSeparator ).getBytes() );
      pr.getOutputStream().flush();
      pr.getOutputStream().write( ( "\\q" + lineSeparator ).getBytes() );
      pr.getOutputStream().flush();
      int i;
      System.out.println( i = pr.waitFor() );
      assertEquals( 0, i );
    } catch ( IOException e ) {
      e.printStackTrace();
      fail( e.toString() );
    } catch ( InterruptedException e ) {
      e.printStackTrace();
      fail( e.toString() );
    }
  }

  /**
   * [PDI-17481] Testing the ability that if no connection is specified, we will mark it as a fail and log the
   * appropriate reason to the user by throwing a KettleException.
   */
  @Test
  public void testNoDatabaseConnection() {
    IngresVectorwiseLoaderData ivwData = new IngresVectorwiseLoaderData();
    IngresVectorwiseLoaderMeta ivwMeta = new IngresVectorwiseLoaderMeta();
    IngresVectorwiseLoaderTest ivwLoader =
            new IngresVectorwiseLoaderTest( stepMockHelper.stepMeta, ivwData, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    assertFalse( ivwLoader.init( ivwMeta, ivwData ) );

    try {
      // Verify that the database connection being set to null throws a KettleException with the following message.
      ivwLoader.verifyDatabaseConnection();
      // If the method does not throw a Kettle Exception, then the DB was set and not null for this test. Fail it.
      fail( "Database Connection is not null, this fails the test." );
    } catch ( KettleException aKettleException ) {
      assertThat( aKettleException.getMessage(), containsString( "There is no connection defined in this step." ) );
    }
  }

  private File createTemplateFile() throws IOException {
    File f = File.createTempFile( IVW_TEMP_PREFIX, IVW_TEMP_EXTENSION );
    // comment deletion for debugging
    f.deleteOnExit();
    return f;
  }

  @SuppressWarnings( "unused" )
  private BaseStep doOutput( List<Object[]> rows, String maxErrorsNumber ) throws KettleException {

    IngresVectorwiseLoaderData ivwData = new IngresVectorwiseLoaderData();
    IngresVectorwiseLoaderTest ivwLoader =
      new IngresVectorwiseLoaderTest( stepMockHelper.stepMeta, ivwData, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );

    DatabaseMeta defMeta = mock( DatabaseMeta.class );
    when( stepMockHelper.processRowsStepMetaInterface.getDatabaseMeta() ).thenReturn( defMeta );
    when( defMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn( "test_table" );
    ivwLoader.init( stepMockHelper.processRowsStepMetaInterface, ivwData );
    RowSet rowSet = stepMockHelper.getMockInputRowSet( rows );
    ivwLoader.addRowSetToInputRowSets( rowSet );

    when( stepMockHelper.processRowsStepMetaInterface.isUsingVwload() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getBufferSize() ).thenReturn( "5000" );
    when( stepMockHelper.processRowsStepMetaInterface.getMaxNrErrors() ).thenReturn( maxErrorsNumber );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldStream() ).thenReturn( fieldStream );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldDatabase() ).thenReturn( fieldStream );
    File errorFile = new File( "/tmp/error.txt" );
    File fifoFile = new File( "/tmp/fifo" );
    try {
      errorFile = createTemplateFile();
      fifoFile = createTemplateFile();
      boolean deleted  = fifoFile.delete();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    when( stepMockHelper.processRowsStepMetaInterface.getErrorFileName() ).thenReturn( errorFile.getPath() );
    when( stepMockHelper.processRowsStepMetaInterface.getFifoFileName() ).thenReturn( fifoFile.getPath() );

    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    ivwLoader.setInputRowMeta( inputRowMeta );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    ValueMetaInteger valueMetaInteger = new ValueMetaInteger();
    ValueMetaString valueMetaString = new ValueMetaString();
    when( inputRowMeta.getValueMeta( 0 ) ).thenReturn( valueMetaInteger );
    when( inputRowMeta.getValueMeta( 1 ) ).thenReturn( valueMetaString );
    when( inputRowMeta.indexOfValue( fieldStream[0] ) ).thenReturn( 0 );
    when( inputRowMeta.indexOfValue( fieldStream[1] ) ).thenReturn( 1 );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    for ( Object[] row1 : rows ) {
      ivwLoader.processRow( stepMockHelper.processRowsStepMetaInterface, ivwData );
    }
    ivwLoader.processRow( stepMockHelper.processRowsStepMetaInterface, ivwData );
    ivwLoader.dispose( stepMockHelper.processRowsStepMetaInterface, ivwData );
    ivwLoader.getLogChannel().snap( Metrics.METRIC_STEP_EXECUTION_STOP );

    return ivwLoader;
  }

}
