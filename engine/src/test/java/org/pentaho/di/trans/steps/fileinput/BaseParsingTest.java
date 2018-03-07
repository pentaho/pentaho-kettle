/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.file.BaseFileField;

/**
 * Base class for all tests for BaseFileInput steps.
 */
@Ignore( "No tests in abstract base class" )
public abstract class BaseParsingTest<Meta extends StepMetaInterface, Data extends StepDataInterface, Step extends BaseStep> {
  protected LogChannelInterface log = new LogChannel( "junit" );
  protected FileSystemManager fs;
  protected String inPrefix;
  protected Meta meta;
  protected Data data;
  protected Step step;
  protected StepMeta stepMeta;
  protected TransMeta transMeta;
  protected Trans trans;

  protected List<Object[]> rows = new ArrayList<>();
  protected int errorsCount;

  /**
   * Initialize step info. Method is final against redefine in descendants.
   */
  @Before
  public final void beforeCommon() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );

    stepMeta = new StepMeta();
    stepMeta.setName( "test" );

    trans = new Trans();
    trans.setLog( log );
    trans.setRunning( true );
    transMeta = new TransMeta() {
      @Override
      public StepMeta findStep( String name ) {
        return stepMeta;
      }
    };

    fs = VFS.getManager();
    inPrefix = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";
  }

  /**
   * Resolve file from test directory.
   */
  protected FileObject getFile( String filename ) throws Exception {
    URL res = this.getClass().getResource( inPrefix + filename );
    assertNotNull( "There is no file", res );
    FileObject file = fs.resolveFile( res.toExternalForm() );
    assertNotNull( "There is no file", file );
    return file;
  }

  /**
   * Declare fields for test.
   */
  protected abstract void setFields( BaseFileField... fields ) throws Exception;

  /**
   * Process all rows.
   */
  protected void process() throws Exception {
    //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
    while ( step.processRow( meta, data ) ) {
      // nothing here - just make sure the rows process
    }
  }

  /**
   * Check result of parsing.
   * 
   * @param expected
   *          array of rows of fields, i.e. { {"field 1 value in row 1","field 2 value in row 1"}, {
   *          "field 1 value in row 2","field 2 value in row 2"} }
   */
  protected void check( Object[][] expected ) throws Exception {
    checkErrors();
    checkRowCount( expected );
  }

  /**
   * Check result no has errors.
   */
  protected void checkErrors() throws Exception {
    assertEquals( "There are errors", 0, errorsCount );
    assertEquals( "There are step errors", 0, step.getErrors() );
  }

  /**
   * Check result of parsing.
   *
   * @param expected
   *          array of rows of fields, i.e. { {"field 1 value in row 1","field 2 value in row 1"}, {
   *          "field 1 value in row 2","field 2 value in row 2"} }
   */
  protected void checkRowCount( Object[][] expected ) throws Exception {
    assertEquals( "Wrong rows count", expected.length, rows.size() );
    checkContent( expected );
  }

  /**
   * Check content of parsing.
   *
   * @param expected
   *          array of rows of fields, i.e. { {"field 1 value in row 1","field 2 value in row 1"}, {
   *          "field 1 value in row 2","field 2 value in row 2"} }
   */
  protected void checkContent( Object[][] expected ) throws Exception {
    for ( int i = 0; i < expected.length; i++ ) {
      assertArrayEquals( "Wrong row: " + Arrays.asList( rows.get( i ) ), expected[i], rows.get( i ) );
    }
  }

  /**
   * Listener for parsing result.
   */
  protected RowListener rowListener = new RowListener() {
    @Override
    public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      rows.add( Arrays.copyOf( row, rowMeta.size() ) );
    }

    @Override
    public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      System.out.println();
    }

    @Override
    public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      errorsCount++;
    }
  };
}
