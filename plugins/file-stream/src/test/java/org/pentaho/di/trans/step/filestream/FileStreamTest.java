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


package org.pentaho.di.trans.step.filestream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowHandler;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.streaming.api.StreamSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith ( MockitoJUnitRunner.class )
public class FileStreamTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  Trans trans = new Trans();
  @Mock TransMeta transMeta;
  @Mock StepMeta stepMeta;
  @Mock StepDataInterface stepData;
  ExecutorService executor = Executors.newSingleThreadExecutor();

  FileStream fileStream;
  FileStreamMeta streamMeta = new FileStreamMeta();
  private File streamFile;
  private FileWriter writer;

  @Before
  public void before() throws IOException, KettleException {
    streamFile = temporaryFolder.newFile();

    writer = new FileWriter( streamFile );

    KettleEnvironment.init();
    trans.setTransMeta( transMeta );
    trans.setLog( new LogChannel( this ) );
    streamMeta.setTransformationPath( getClass().getResource( "/subtrans.ktr" ).getPath() );
    streamMeta.setBatchDuration( "5" );
    streamMeta.setBatchSize( "1" );
    streamMeta.setSourcePath( streamFile.getPath() );

    when( stepMeta.getName() ).thenReturn( "mocked name" );
    when( stepMeta.getStepMetaInterface() ).thenReturn( streamMeta );
    when( transMeta.findStep( any() ) ).thenReturn( stepMeta );

  }

  @After
  public void after() throws IOException {
    writer.close();
  }


  @Ignore
  @Test public void testStreamFile() throws IOException {
    FileStream step =
      (FileStream) streamMeta.getStep( stepMeta, stepData, 1, transMeta, trans );
    step.init( streamMeta, new FileStreamData() );


    final RowHandler rowHandler = getRowHandler();
    step.setRowHandler( rowHandler );

    StreamSource<List<Object>> source = step.getStreamSource();
    source.open();

    writer.write( "line 1" );
    writer.flush();
    //Thread.sleep( 2000 );
    executor.submit( () -> {
      int i = 0;
      while ( true ) {
        Thread.sleep( 100 );
        writer.write( "line" + i++ + "\n" );
        writer.flush();
      }
    } );

    Iterator iter = source.observable().blockingIterable().iterator();

    for ( int i = 0; i < 10; i++ ) {

      System.out.println( iter.next() );
    }
    source.close();
  }

  List<Object[]> rows = new ArrayList<>();

  private RowHandler getRowHandler() {
    return new RowHandler() {


      @Override public Object[] getRow() {
        return rows.size() > 0 ? rows.remove( 0 ) : null;
      }

      @Override public void putRow( RowMetaInterface rowMeta, Object[] row ) {
        rows.add( row );
      }

      @Override public void putError( RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions,
                                      String fieldNames, String errorCodes ) {

      }
    };
  }
}
