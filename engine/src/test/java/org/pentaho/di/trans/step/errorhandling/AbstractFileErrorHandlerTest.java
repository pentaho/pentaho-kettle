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

package org.pentaho.di.trans.step.errorhandling;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;

import java.io.File;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AbstractFileErrorHandlerTest {

  private static final String DATE_FORMAT = "ddMMyyyy-HHmmss";
  private static final String DEFAULT_ENCODING = "UTF8";
  private final SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );
  private AbstractFileErrorHandler abstractFileErrorHandler;

  @Test
  public void getWriterNullEncoding() throws Exception {
    Object source = Mockito.mock( Object.class );
    setupErrorHandler( null, source );
    OutputStreamWriter outputStreamWriter = (OutputStreamWriter) abstractFileErrorHandler.getWriter( source );
    assertEquals( DEFAULT_ENCODING, outputStreamWriter.getEncoding() );
  }

  @Test
  public void getWriterEmptyEncoding() throws Exception {
    Object source = Mockito.mock( Object.class );
    setupErrorHandler( "", source );
    OutputStreamWriter outputStreamWriter = (OutputStreamWriter) abstractFileErrorHandler.getWriter( source );
    assertEquals( DEFAULT_ENCODING, outputStreamWriter.getEncoding() );
  }

  private void setupErrorHandler( String encoding, Object source ) throws Exception {
    File tempFile = File.createTempFile( "test", "file.txt" );
    tempFile.deleteOnExit();
    FileObject fileObject = KettleVFS.getFileObject( tempFile.getAbsolutePath() );
    Date date = new Date();
    String destDirectory = tempFile.getParent();
    String fileExtension = ".txt";
    BaseStep baseStep = Mockito.mock( BaseStep.class );
    TransMeta transMeta = Mockito.mock( TransMeta.class );

    when( baseStep.getTransMeta() ).thenReturn( transMeta );
    when( transMeta.getName() ).thenReturn( "TestStep" );
    when( baseStep.getStepname() ).thenReturn( "Test Step Name" );
    abstractFileErrorHandler = spy( new FileErrorHandlerContentLineNumber( date, destDirectory, fileExtension, encoding, baseStep ) );
    abstractFileErrorHandler.handleFile( fileObject );
    when( source.toString() ).thenReturn( "testFile.txt" );
    when( AbstractFileErrorHandler.getReplayFilename( destDirectory, "testFile", dateFormat.format( date ), fileExtension, source ) ).thenReturn( null );
  }
}
